package com.oppo.cloud.diagnosis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.constant.ComponentEnum;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.flink.JobManagerConfigItem;
import com.oppo.cloud.common.domain.flink.enums.*;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.common.util.elastic.BulkApi;
import com.oppo.cloud.diagnosis.advice.DiagnosisDoctor;
import com.oppo.cloud.diagnosis.config.EsConfig;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.diagnosis.service.DiagnosisService;
import com.oppo.cloud.diagnosis.service.FlinkMetaService;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.mapper.FlinkTaskDiagnosisMapper;
import com.oppo.cloud.mapper.FlinkTaskDiagnosisRuleAdviceMapper;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.JOB_UP_TIME;

/**
 * 诊断服务
 */
@Service
@Slf4j
public class DiagnosisServiceImpl implements DiagnosisService {
    @Autowired
    FlinkDiagnosisMetricsServiceImpl flinkDiagnosisMetricsServiceImpl;
    @Autowired
    DiagnosisDoctor diagnosisDoctor;
    @Autowired
    FlinkMetaService flinkMetaService;
    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;
    @Autowired
    BlocklistMapper blocklistMapper;
    @Autowired
    FlinkTaskDiagnosisMapper flinkTaskDiagnosisMapper;
    @Autowired
    FlinkTaskDiagnosisRuleAdviceMapper flinkTaskDiagnosisRuleAdviceMapper;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;
    @Autowired
    DiagnosisParamsConstants cons;
    @Resource
    private RestHighLevelClient elasticClient;
    @Autowired
    EsConfig esConfig;
    public static final String REALTIME_DIAGNOSIS_REPORT_ES_INDEX_PREFIX = "realtime-report-";
    private static final String YARN_APP_URL = "http://%s:%s/ws/v1/cluster/apps/%s";
    private static final String FLINK_JOB_MANAGER_CONFIG = "%s/jobmanager/config";

    /**
     * 添加诊断类型
     *
     * @param realtimeTaskDiagnosis
     * @param code
     */
    private void addDiagnosisTypes(RealtimeTaskDiagnosis realtimeTaskDiagnosis, int code) {
        String types = realtimeTaskDiagnosis.getDiagnosisTypes();
        List<Integer> typesList = JSON.parseArray(types, Integer.class);
        if (typesList == null) {
            typesList = new ArrayList<>();
        }
        typesList.add(code);
        String newTypes = JSON.toJSONString(typesList);
        realtimeTaskDiagnosis.setDiagnosisTypes(newTypes);
    }

    /**
     * 添加诊断资源类型
     *
     * @param realtimeTaskDiagnosis
     * @param code
     */
    private void addDiagnosisResourceTypes(RealtimeTaskDiagnosis realtimeTaskDiagnosis, int code) {
        String types = realtimeTaskDiagnosis.getDiagnosisResourceType();
        List<Integer> typesList = JSON.parseArray(types, Integer.class);
        if (typesList == null) {
            typesList = new ArrayList<>();
        }
        typesList.add(code);
        String newTypes = JSON.toJSONString(typesList);
        realtimeTaskDiagnosis.setDiagnosisResourceType(newTypes);
    }

    public void updateRealtimeTaskAppStatus(RealtimeTaskApp realtimeTaskApp) {
        // 检查app 是否在运行状态
        String appId = realtimeTaskApp.getApplicationId();
        YarnApp app = flinkMetaService.requestYarnApp(appId);
        if (app == null) {
            log.info("没有找到 app {}", appId);
            return;
        }
        // 如果检测到app状态是 FINISHED,FAILED,KILLED,则标记元数据状态为finish
        if (
                app.getState().equalsIgnoreCase(YarnApplicationState.FINISHED.getDesc()) ||
                        app.getState().equalsIgnoreCase(YarnApplicationState.FAILED.getDesc()) ||
                        app.getState().equalsIgnoreCase(YarnApplicationState.KILLED.getDesc())
        ) {
            realtimeTaskApp.setTaskState(RealtimeTaskAppState.FINISHED.getDesc());
            flinkTaskAppMapper.updateByPrimaryKey(realtimeTaskApp);
            log.info(" app 已经停止 {}", appId);
        }
    }

    /**
     * 诊断作业
     *
     * @param realtimeTaskApp
     * @param start           秒
     * @param end             秒
     * @param from
     * @return
     */
    @Override
    public RealtimeTaskDiagnosis diagnosisApp(RealtimeTaskApp realtimeTaskApp, long start, long end,
                                              DiagnosisFrom from) {
        try {
            // 黑名单检查
            BlocklistExample blocklistExample = new BlocklistExample();
            BlocklistExample.Criteria criteria = blocklistExample.createCriteria()
                    .andTaskNameEqualTo(realtimeTaskApp.getTaskName())
                    .andFlowNameEqualTo(realtimeTaskApp.getFlowName())
                    .andProjectNameEqualTo(realtimeTaskApp.getProjectName())
                    .andComponentEqualTo(ComponentEnum.Realtime.getDes())
                    .andDeletedEqualTo(0);
            List<Blocklist> blockLists = blocklistMapper.selectByExample(blocklistExample);
            log.debug(blocklistExample.getOredCriteria().toString());
            if (blockLists != null && blockLists.size() > 0) {
                log.debug("白名单拦截:{}", realtimeTaskApp);
                return null;
            } else {
                log.debug("白名单通过:{}", realtimeTaskApp);
            }
            RcJobDiagnosis rcJobDiagnosis = new RcJobDiagnosis();
            RealtimeTaskDiagnosis realtimeTaskDiagnosis = new RealtimeTaskDiagnosis();
            // 元数据填到诊断结果中
            fillRealtimeTaskDiagnosisWithTaskMeta(realtimeTaskDiagnosis, realtimeTaskApp);
            // 从flink ui config 获取 jobName,运行配置参数,如果访问不到，说明作业有问题
            List<JobManagerConfigItem> flinkConfigItems = flinkMetaService.reqFlinkConfig(realtimeTaskApp.getFlinkTrackUrl());
            if (flinkConfigItems == null) {
                log.info("flink ui无法访问 {}", realtimeTaskApp.getFlinkTrackUrl());
                return null;
            }
            String jobId = flinkMetaService.getJobId(realtimeTaskApp.getFlinkTrackUrl());
            if (jobId == null) {
                log.info("flink jobid 获取失败 {}", realtimeTaskApp.getFlinkTrackUrl());
                return null;
            }
            List<String> tmIds = flinkMetaService.getTmIds(realtimeTaskApp.getFlinkTrackUrl());
            // 通过flink job manager的配置更新元数据
            flinkMetaService.fillFlinkMetaWithFlinkConfigOnYarn(realtimeTaskApp, flinkConfigItems, jobId);
            // 更新元数据
            flinkTaskAppMapper.updateByPrimaryKeySelective(realtimeTaskApp);
            // 元数据更新后填到诊断结果中
            fillRealtimeTaskDiagnosisWithTaskMeta(realtimeTaskDiagnosis, realtimeTaskApp);
            // 构造诊断上下文，元数据填到上下文中
            fillRcJobDiagnosisWithTaskMeta(rcJobDiagnosis, realtimeTaskApp);
            // 构造context
            DiagnosisContext context = new DiagnosisContext(rcJobDiagnosis, start, end, flinkDiagnosisMetricsServiceImpl, from);
            context.getMessages().put(DiagnosisParam.JobId, jobId);
            context.getMessages().put(DiagnosisParam.TmIds, tmIds);
            // 执行诊断
            diagnosisDoctor.diagnosis(context);
            if (context.getStopResourceDiagnosis()) {
                log.info("不满足诊断条件，返回null");
                return null;
            }
            // 诊断结果回填到realtimeTaskDiagnosis中
            fillRealtimeTaskDiagnosisWithDiagnosisContext(realtimeTaskDiagnosis, context);
            // 存储数据
            // 存储realtimeTaskDiagnosis
            flinkTaskDiagnosisMapper.insertSelective(realtimeTaskDiagnosis);
            // 构造 RealtimeTaskDiagnosisRuleAdvice,填入realtimeTaskDiagnosis的id
            List<RcJobDiagnosisAdvice> advices = context.getAdvices();
            List<Map<String, Object>> reportEsList = new ArrayList<>();
            for (RcJobDiagnosisAdvice advice : advices) {
                RealtimeTaskDiagnosisRuleAdvice realtimeTaskDiagnosisRuleAdvice = new RealtimeTaskDiagnosisRuleAdvice();
                realtimeTaskDiagnosisRuleAdvice.setRealtimeTaskDiagnosisId(realtimeTaskDiagnosis.getId());
                realtimeTaskDiagnosisRuleAdvice.setRuleName(advice.getRuleName());
                realtimeTaskDiagnosisRuleAdvice.setRuleType(advice.getAdviceType().getCode());
                realtimeTaskDiagnosisRuleAdvice.setHasAdvice(advice.getHasAdvice() ? (short) 1 : (short) 0);
                realtimeTaskDiagnosisRuleAdvice.setDescription(advice.getAdviceDescription());
                realtimeTaskDiagnosisRuleAdvice.setCreateTime(new Date());
                realtimeTaskDiagnosisRuleAdvice.setUpdateTime(new Date());
                flinkTaskDiagnosisRuleAdviceMapper.insertSelective(realtimeTaskDiagnosisRuleAdvice);
                DiagnosisRuleReport diagnosisRuleReport = advice.getDiagnosisRuleReport();
                String reportJson = "";
                if (diagnosisRuleReport != null) {
                    reportJson = JSON.toJSONString(diagnosisRuleReport);
                }
                Map<String, Object> reportDoc = new HashMap<>();
                reportDoc.put("doc_id", realtimeTaskDiagnosisRuleAdvice.getId().toString());
                reportDoc.put("report", reportJson);
                reportDoc.put("ts", LocalDateTime.now());
                reportEsList.add(reportDoc);
            }
            BulkResponse response;
            try {
                log.info("es config:{} {} {}", esConfig.getHosts(), esConfig.getUsername(), esConfig.getPassword());
                response = BulkApi.bulk(elasticClient, REALTIME_DIAGNOSIS_REPORT_ES_INDEX_PREFIX, reportEsList);
                BulkItemResponse[] responses = response.getItems();
                for (BulkItemResponse r : responses) {
                    if (r.isFailed()) {
                        log.info("failedInsertApp:{},{}", r.getId(), r.status());
                    }
                }
                log.info("realtime diagnosis save count:,{}", reportEsList.size());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }

            // 返回数据
            return realtimeTaskDiagnosis;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return null;
        }
    }

    /**
     * 诊断所有作业
     *
     * @param start
     * @param end
     * @param from
     */
    @Override
    public void diagnosisAllApp(long start, long end, DiagnosisFrom from) {
        RealtimeTaskAppExample realtimeTaskAppExample = new RealtimeTaskAppExample();
        realtimeTaskAppExample.createCriteria()
                .andTaskStateEqualTo(RealtimeTaskAppState.RUNNING.getDesc())
        ;
        long count = flinkTaskAppMapper.countByExample(realtimeTaskAppExample);
        int pageSize = 100;
        int pageNum = (int) Math.ceil((double) count / pageSize);
        for (int i = 1; i <= pageNum; i++) {
            PageHelper.startPage(i, pageSize);
            try {
                List<RealtimeTaskApp> realtimeTaskApps = flinkTaskAppMapper.selectByExample(realtimeTaskAppExample);
                for (RealtimeTaskApp realtimeTaskApp : realtimeTaskApps) {
                    diagnosisApp(realtimeTaskApp, start, end, from);
                    updateRealtimeTaskAppStatus(realtimeTaskApp);
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }

    }

    /**
     * 检测小时级诊断是否需要进行
     *
     * @param job
     * @param start
     * @param end
     * @return
     */
    private Boolean checkNeedDiagnosisHourly(RealtimeTaskApp job, long start, long end) {
        String metricJobName = job.getJobName();
        String jobId = flinkMetaService.getJobId(job.getFlinkTrackUrl());
        String jobUpTime = flinkDiagnosisMetricsServiceImpl.addLabel(JOB_UP_TIME, "job_id", jobId);
        List<MetricResult.DataResult> jobUpTimeMetrics = flinkDiagnosisMetricsServiceImpl
                .getJobMetrics(jobUpTime, start, end);
        // 首先，如果jobUpTimeMetrics返回不止一个list，那么肯定不在上线的一小时后,因为小时诊断只诊断一小时内的数据
        if (jobUpTimeMetrics == null || jobUpTimeMetrics.size() != 1) {
            log.info("小时级别诊断:{} 初始化时间为空或者大小不为1", jobUpTime);
            return false;
        }
        double minUpTime = monitorMetricUtil.getKeyValueStream(jobUpTimeMetrics.get(0))
                .get()
                .map(MetricResult.KeyValue::getValue)
                .filter(Objects::nonNull)
                .filter(x -> x > 0)
                .mapToDouble(x -> x)
                .min()
                .orElse(-1);
        int diagnosisStartMinutes = cons.hourlyDiagnosisStartMinutes;
        int diagnosisEndMinutes = cons.hourlyDiagnosisEndMinutes;
        // 如果minUpTime 上报为-1，也不诊断，需要修复上报正确时间
        if (minUpTime / 1000 / 60 > diagnosisStartMinutes && minUpTime / 1000 / 60 < diagnosisEndMinutes) {
            log.info("小时级别诊断:" + metricJobName +
                    String.format("诊断开始时,上线时间%.2f分钟,在%d分钟到%d分钟内", minUpTime / 1000 / 60, diagnosisStartMinutes, diagnosisEndMinutes));
        } else if (minUpTime == -1) {
            log.info(String.format("小时级别诊断:%s,上线时间指标全部为-1或者为0", metricJobName));
            return false;
        } else {
            log.info("小时级别诊断:" + metricJobName +
                    String.format("诊断开始时,上线时间%.2f分钟,不在%d分钟到%d分钟内,不诊断", minUpTime / 1000 / 60, diagnosisStartMinutes, diagnosisEndMinutes));
            return false;
        }
        return true;
    }


    /**
     * 任务上线诊断,任务上线1小时候开始诊断
     *
     * @param start
     * @param end
     * @param from
     */
    @Override
    public void diagnosisAppHourly(long start, long end, DiagnosisFrom from) {
        RealtimeTaskAppExample realtimeTaskAppExample = new RealtimeTaskAppExample();
        realtimeTaskAppExample.createCriteria()
                .andTaskStateEqualTo(RealtimeTaskAppState.RUNNING.getDesc())
        ;
        long count = flinkTaskAppMapper.countByExample(realtimeTaskAppExample);
        int pageSize = 100;
        int pageNum = (int) Math.ceil((double) count / pageSize);
        for (int i = 1; i <= pageNum; i++) {
            PageHelper.startPage(i, pageSize);
            try {
                List<RealtimeTaskApp> realtimeTaskApps = flinkTaskAppMapper.selectByExample(realtimeTaskAppExample);
                for (RealtimeTaskApp realtimeTaskApp : realtimeTaskApps) {
                    if (checkNeedDiagnosisHourly(realtimeTaskApp, start, end)) {
                        diagnosisApp(realtimeTaskApp, start, end, from);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }

    }

    /**
     * 填充元数据
     *
     * @param rcJobDiagnosis
     * @param realtimeTaskApp
     */
    private void fillRcJobDiagnosisWithTaskMeta(RcJobDiagnosis rcJobDiagnosis, RealtimeTaskApp realtimeTaskApp) {
        rcJobDiagnosis.setJobName(realtimeTaskApp.getJobName());
        rcJobDiagnosis.setParallel(realtimeTaskApp.getParallel());
        rcJobDiagnosis.setTmSlotNum(realtimeTaskApp.getTmSlot());
        if (realtimeTaskApp.getParallel() != null && realtimeTaskApp.getTmSlot() != null) {
            rcJobDiagnosis.setTmNum((int) Math.ceil((double) realtimeTaskApp.getParallel() / realtimeTaskApp.getTmSlot()));
        }
        rcJobDiagnosis.setTmMem(realtimeTaskApp.getTmMem());
        rcJobDiagnosis.setJmMem(realtimeTaskApp.getJmMem());
        rcJobDiagnosis.setTmCore(realtimeTaskApp.getTmCore());
    }

    /**
     * 填充元数据
     *
     * @param realtimeTaskDiagnosis
     * @param realtimeTaskApp
     */
    private void fillRealtimeTaskDiagnosisWithTaskMeta(RealtimeTaskDiagnosis realtimeTaskDiagnosis, RealtimeTaskApp realtimeTaskApp) {
        realtimeTaskDiagnosis.setRealtimeTaskAppId(realtimeTaskApp.getId());
        realtimeTaskDiagnosis.setUsername(realtimeTaskApp.getUsername());
        realtimeTaskDiagnosis.setUserId(realtimeTaskApp.getUserId());
        realtimeTaskDiagnosis.setProjectName(realtimeTaskApp.getProjectName());
        realtimeTaskDiagnosis.setProjectId(realtimeTaskApp.getProjectId());
        realtimeTaskDiagnosis.setFlowName(realtimeTaskApp.getFlowName());
        realtimeTaskDiagnosis.setFlowId(realtimeTaskApp.getFlowId());
        realtimeTaskDiagnosis.setTaskName(realtimeTaskApp.getTaskName());
        realtimeTaskDiagnosis.setTaskId(realtimeTaskApp.getTaskId());
        realtimeTaskDiagnosis.setApplicationId(realtimeTaskApp.getApplicationId());
        realtimeTaskDiagnosis.setFlinkTrackUrl(realtimeTaskApp.getFlinkTrackUrl());
        realtimeTaskDiagnosis.setAllocatedMb(realtimeTaskApp.getAllocatedMb());
        realtimeTaskDiagnosis.setAllocatedVcores(realtimeTaskApp.getAllocatedVcores());
        realtimeTaskDiagnosis.setRunningContainers(realtimeTaskApp.getRunningContainers());
        realtimeTaskDiagnosis.setEngineType(realtimeTaskApp.getEngineType());
        realtimeTaskDiagnosis.setExecutionTime(realtimeTaskApp.getExecutionTime());
        realtimeTaskDiagnosis.setDuration(realtimeTaskApp.getDuration());
        realtimeTaskDiagnosis.setStartTime(realtimeTaskApp.getStartTime());
        realtimeTaskDiagnosis.setEndTime(realtimeTaskApp.getEndTime());
        realtimeTaskDiagnosis.setVcoreSeconds(realtimeTaskApp.getVcoreSeconds());
        realtimeTaskDiagnosis.setMemorySeconds(realtimeTaskApp.getMemorySeconds());
        realtimeTaskDiagnosis.setQueue(realtimeTaskApp.getQueue());
        realtimeTaskDiagnosis.setClusterName(realtimeTaskApp.getClusterName());
        realtimeTaskDiagnosis.setRetryTimes(realtimeTaskApp.getRetryTimes());
        realtimeTaskDiagnosis.setExecuteUser(realtimeTaskApp.getExecuteUser());
        realtimeTaskDiagnosis.setDiagnosis(realtimeTaskApp.getDiagnosis());
        realtimeTaskDiagnosis.setJobName(realtimeTaskApp.getJobName());
    }

    /**
     * 填充数据
     *
     * @param realtimeTaskDiagnosis
     * @param context
     */
    private void fillRealtimeTaskDiagnosisWithDiagnosisContext(RealtimeTaskDiagnosis realtimeTaskDiagnosis, DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        realtimeTaskDiagnosis.setParallel(rcJobDiagnosis.getParallel());
        realtimeTaskDiagnosis.setTmSlot(rcJobDiagnosis.getTmSlotNum());
        realtimeTaskDiagnosis.setTmCore(rcJobDiagnosis.getTmCore());
        realtimeTaskDiagnosis.setTmMem(rcJobDiagnosis.getTmMem());
        realtimeTaskDiagnosis.setJmMem(rcJobDiagnosis.getJmMem());
        realtimeTaskDiagnosis.setTmNum(rcJobDiagnosis.getTmNum());
        realtimeTaskDiagnosis.setDiagnosisEndTime(new Date(context.getEnd() * 1000));
        realtimeTaskDiagnosis.setDiagnosisStartTime(new Date(context.getStart() * 1000));
        realtimeTaskDiagnosis.setDiagnosisFrom(context.getFrom().getCode());
        realtimeTaskDiagnosis.setDiagnosisParallel(rcJobDiagnosis.getDiagnosisParallel());
        realtimeTaskDiagnosis.setDiagnosisJmMemSize(rcJobDiagnosis.getDiagnosisJmMem());
        realtimeTaskDiagnosis.setDiagnosisTmMemSize(rcJobDiagnosis.getDiagnosisTmMem());
        realtimeTaskDiagnosis.setDiagnosisTmSlotNum(rcJobDiagnosis.getDiagnosisTmSlot());
        realtimeTaskDiagnosis.setDiagnosisTmCoreNum(rcJobDiagnosis.getDiagnosisTmCore());
        realtimeTaskDiagnosis.setDiagnosisTmNum(rcJobDiagnosis.getDiagnosisTmNum());
        realtimeTaskDiagnosis.setCreateTime(new Date());
        realtimeTaskDiagnosis.setUpdateTime(new Date());
        // 资源调优类型
        if (realtimeTaskDiagnosis.getTmNum() != null && realtimeTaskDiagnosis.getTmCore() != null) {
            int preCore = realtimeTaskDiagnosis.getTmNum() * realtimeTaskDiagnosis.getTmCore();
            int newCore = realtimeTaskDiagnosis.getDiagnosisTmNum() * realtimeTaskDiagnosis.getDiagnosisTmCoreNum();
            // 判断资源变化
            if (newCore > preCore) {
                addDiagnosisResourceTypes(realtimeTaskDiagnosis, DiagnosisResourceType.INCR_CPU.getCode());
            } else if (newCore < preCore) {
                addDiagnosisResourceTypes(realtimeTaskDiagnosis, DiagnosisResourceType.DECR_CPU.getCode());
            }
        }
        // 资源调优类型
        if (realtimeTaskDiagnosis.getTmNum() != null && realtimeTaskDiagnosis.getTmMem() != null) {
            int preMem = realtimeTaskDiagnosis.getTmNum() * realtimeTaskDiagnosis.getTmMem();
            int newMem = realtimeTaskDiagnosis.getDiagnosisTmNum() * realtimeTaskDiagnosis.getDiagnosisTmMemSize();
            // 判断资源变化
            if (newMem > preMem) {
                addDiagnosisResourceTypes(realtimeTaskDiagnosis, DiagnosisResourceType.INCR_MEM.getCode());
            } else if (newMem < preMem) {
                addDiagnosisResourceTypes(realtimeTaskDiagnosis, DiagnosisResourceType.DECR_MEM.getCode());
            }
        }

        // 诊断规则类型
        List<RcJobDiagnosisAdvice> advices = context.getAdvices();
        for (RcJobDiagnosisAdvice rcJobDiagnosisAdvice : advices) {
            if (rcJobDiagnosisAdvice.getHasAdvice()) {
                if (rcJobDiagnosisAdvice.getAdviceType().getCode() == DiagnosisRuleType.RuntimeExceptionRule.getCode()) {
                    addDiagnosisResourceTypes(realtimeTaskDiagnosis, DiagnosisResourceType.RUNTIME_EXCEPTION.getCode());
                }
                addDiagnosisTypes(realtimeTaskDiagnosis, rcJobDiagnosisAdvice.getAdviceType().getCode());
            }
        }
    }
}
