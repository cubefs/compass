/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.flink.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.constant.ComponentEnum;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.elasticsearch.FlinkTaskAdvice;
import com.oppo.cloud.common.domain.elasticsearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.SimpleUser;
import com.oppo.cloud.common.domain.flink.JobManagerConfigItem;
import com.oppo.cloud.common.domain.flink.enums.*;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.elastic.BulkApi;
import com.oppo.cloud.flink.advice.DiagnosisDoctor;
import com.oppo.cloud.flink.config.EsConfig;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.flink.service.DiagnosisService;
import com.oppo.cloud.flink.service.FlinkElasticSearchService;
import com.oppo.cloud.flink.service.FlinkMetaService;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.mapper.FlinkTaskDiagnosisRuleAdviceMapper;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.JOB_UP_TIME;

/**
 * 诊断服务
 */
@Service
@Slf4j
public class DiagnosisServiceImpl implements DiagnosisService {

    @Autowired
    private FlinkDiagnosisMetricsServiceImpl flinkDiagnosisMetricsServiceImpl;

    @Autowired
    private DiagnosisDoctor diagnosisDoctor;

    @Autowired
    private FlinkMetaService flinkMetaService;

    @Autowired
    private FlinkTaskAppMapper flinkTaskAppMapper;

    @Autowired
    private BlocklistMapper blocklistMapper;

    @Autowired
    private FlinkTaskDiagnosisRuleAdviceMapper flinkTaskDiagnosisRuleAdviceMapper;

    @Autowired
    private MonitorMetricUtil monitorMetricUtil;

    @Autowired
    private DiagnosisParamsConstants cons;

    @Resource(name = "flinkElasticClient")
    private RestHighLevelClient elasticClient;

    @Autowired
    private EsConfig esConfig;

    @Value("${custom.elasticsearch.flinkReportIndex.name}")
    private String flinkReportIndex;

    @Value("${custom.elasticsearch.flinkTaskAnalysisIndex.name}")
    private String flinkTaskAnalysisIndex;

    @Autowired
    private FlinkElasticSearchService flinkElasticSearchService;

    /**
     * 添加诊断类型
     *
     * @param flinkTaskAnalysis
     * @param ruleAlias
     */
    private void addDiagnosisTypes(FlinkTaskAnalysis flinkTaskAnalysis, String ruleAlias) {
        List<String> types = flinkTaskAnalysis.getDiagnosisTypes();
        if (types == null) {
            types = new ArrayList<>();
        }
        types.add(ruleAlias);
        flinkTaskAnalysis.setDiagnosisTypes(types);
    }

    /**
     * 添加诊断资源类型
     *
     * @param flinkTaskAnalysis
     * @param code
     */
    private void addDiagnosisResourceTypes(FlinkTaskAnalysis flinkTaskAnalysis, int code) {
        List<Integer> types = flinkTaskAnalysis.getDiagnosisResourceType();
        if (types == null) {
            types = new ArrayList<>();
        }
        types.add(code);
        flinkTaskAnalysis.setDiagnosisResourceType(types);
    }

    public void updateRealtimeTaskAppStatus(FlinkTaskApp flinkTaskApp) {
        //通过 tracking url 检查
        List<JobManagerConfigItem> jobManagerConfigItems = flinkMetaService.reqFlinkConfig(flinkTaskApp.getFlinkTrackUrl());
        if (System.currentTimeMillis() - flinkTaskApp.getCreateTime().getTime() > 1000 * 60 * 30 && jobManagerConfigItems == null) {
            log.info("作业访问tracking url失败 {}", flinkTaskApp);
            flinkTaskApp.setTaskState(FlinkTaskAppState.FINISHED.getDesc());
            flinkTaskAppMapper.updateByPrimaryKey(flinkTaskApp);
            return;
        }

        // 检查app 是否在运行状态
        String appId = flinkTaskApp.getApplicationId();
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
            flinkTaskApp.setTaskState(FlinkTaskAppState.FINISHED.getDesc());
            flinkTaskAppMapper.updateByPrimaryKey(flinkTaskApp);
            log.info(" app 已经停止 {}", appId);
        }
    }

    /**
     * 诊断作业
     *
     * @param flinkTaskApp
     * @param start        秒
     * @param end          秒
     * @param from
     * @return
     */
    @Override
    public FlinkTaskAnalysis diagnosisApp(FlinkTaskApp flinkTaskApp, long start, long end,
                                          DiagnosisFrom from) throws Exception {
        // 白名单检查
        BlocklistExample example = new BlocklistExample();
        example.createCriteria()
                .andTaskNameEqualTo(flinkTaskApp.getTaskName())
                .andFlowNameEqualTo(flinkTaskApp.getFlowName())
                .andProjectNameEqualTo(flinkTaskApp.getProjectName())
                .andComponentEqualTo(ComponentEnum.Realtime.getDes())
                .andDeletedEqualTo(0);

        List<Blocklist> blockLists = blocklistMapper.selectByExample(example);
        log.debug(example.getOredCriteria().toString());
        if (blockLists != null && blockLists.size() > 0) {
            log.debug("白名单拦截:{}", flinkTaskApp);
            return null;
        } else {
            log.debug("白名单通过:{}", flinkTaskApp);
        }
        RcJobDiagnosis rcJobDiagnosis = new RcJobDiagnosis();
        FlinkTaskAnalysis flinkTaskAnalysis = new FlinkTaskAnalysis();
        flinkTaskAnalysis.setDiagnosisResourceType(new ArrayList<>());

        // 1、元数据填到诊断结果中
        // 从flink ui config 获取 jobName,运行配置参数,如果访问不到，说明作业有问题
        List<JobManagerConfigItem> flinkConfigItems = flinkMetaService.reqFlinkConfig(flinkTaskApp.getFlinkTrackUrl());
        if (flinkConfigItems == null) {
            log.info("flink ui无法访问 {}", flinkTaskApp.getFlinkTrackUrl());
            return null;
        }
        String jobId = flinkMetaService.getJobId(flinkTaskApp.getFlinkTrackUrl());
        if (jobId == null) {
            log.info("flink jobid 获取失败 {}", flinkTaskApp.getFlinkTrackUrl());
            return null;
        }
        List<String> tmIds = flinkMetaService.getTmIds(flinkTaskApp.getFlinkTrackUrl());
        // 通过flink job manager的配置更新元数据
        flinkMetaService.fillFlinkMetaWithFlinkConfigOnYarn(flinkTaskApp, flinkConfigItems, jobId);
        // 更新元数据
        flinkTaskAppMapper.updateByPrimaryKeySelective(flinkTaskApp);
        // 元数据更新后填到诊断结果中
        fillFlinkTaskAnalysisWithTaskMeta(flinkTaskAnalysis, flinkTaskApp);
        // 构造诊断上下文，元数据填到上下文中
        fillRcJobDiagnosisWithTaskMeta(rcJobDiagnosis, flinkTaskApp);
        // 构造context
        DiagnosisContext context = new DiagnosisContext(rcJobDiagnosis, start, end, flinkDiagnosisMetricsServiceImpl, from);
        context.getMessages().put(DiagnosisParam.JobId, jobId);
        context.getMessages().put(DiagnosisParam.TmIds, tmIds);

        // 2、执行诊断阶段
        diagnosisDoctor.diagnosis(context);
        if (context.getStopResourceDiagnosis()) {
            log.info("不满足诊断条件，返回null");
            return null;
        }
        // 诊断结果回填到FlinkTaskDiagnosis中
        fillFlinkTaskAnalysisWithDiagnosisContext(flinkTaskAnalysis, context);
        // 3、存储数据阶段
        // 3.1 存储flinkTaskAnalysis
        // 构造 FlinkTaskDiagnosisRuleAdvice, 填入FlinkTaskDiagnosis的id
        List<RcJobDiagnosisAdvice> advices = context.getAdvices();
        List<String> reports = new ArrayList<>();
        List<FlinkTaskAdvice> flinkTaskAdvices = new ArrayList<>();

        String index = flinkTaskAnalysis.genIndex(flinkTaskAnalysisIndex);
        String id = flinkTaskAnalysis.genDocId();

        for (RcJobDiagnosisAdvice advice : advices) {
            FlinkTaskAdvice flinkTaskAdvice = new FlinkTaskAdvice();

            flinkTaskAdvice.setRuleName(advice.getRuleName());
            flinkTaskAdvice.setRuleCode(advice.getAdviceType().getCode());
            flinkTaskAdvice.setRuleAlias(advice.getAdviceType().getName());
            flinkTaskAdvice.setHasAdvice(advice.getHasAdvice() ? 1 : 0);
            flinkTaskAdvice.setDescription(advice.getAdviceDescription());
            flinkTaskAdvices.add(flinkTaskAdvice);

            DiagnosisRuleReport diagnosisRuleReport = advice.getDiagnosisRuleReport();
            if (diagnosisRuleReport == null) {
                continue;
            }

            String reportJson = JSON.toJSONString(diagnosisRuleReport);
            Map<String, Object> report = new HashMap<>();
            report.put("flinkTaskAnalysisId", id);
            report.put("reportJson", reportJson);
            report.put("createTime", flinkTaskAnalysis.getCreateTime());

            reports.add(JSON.toJSONString(report));
        }

        flinkTaskAnalysis.setAdvices(flinkTaskAdvices);
        Set<Integer> resourceTypeSet = new HashSet<>();
        resourceTypeSet.addAll(flinkTaskAnalysis.getDiagnosisResourceType());

        int totalCores = flinkTaskAnalysis.getTmCore() * flinkTaskAnalysis.getTmNum();
        int totalMem = flinkTaskAnalysis.getTmMemory() * flinkTaskAnalysis.getTmNum();

        flinkTaskAnalysis.setTotalCoreNum((long) totalCores);
        flinkTaskAnalysis.setTotalMemNum((long) totalMem);
        flinkTaskAnalysis.setCutCoreNum(0L);
        flinkTaskAnalysis.setCutMemNum(0L);

        if (resourceTypeSet.contains(2)) { // 2缩减cpu
            int cutCores = totalCores - flinkTaskAnalysis.getDiagnosisTmCoreNum() * flinkTaskAnalysis.getDiagnosisTmNum();
            flinkTaskAnalysis.setCutCoreNum((long) cutCores);
        }

        if (resourceTypeSet.contains(3)) { // 3缩减mem
            int cutMemory = totalMem - flinkTaskAnalysis.getDiagnosisTmMemory() * flinkTaskAnalysis.getDiagnosisTmNum();
            flinkTaskAnalysis.setCutMemNum((long) cutMemory);
        }

        log.info("result=>" + flinkTaskAnalysis);


        UpdateResponse update = flinkElasticSearchService.insertOrUpDateEs(index, id, flinkTaskAnalysis.genDoc());
        flinkTaskAnalysis.setDocId(id);
        log.info("update:" + update);

        BulkResponse response;
        try {
            String reportIndex = flinkReportIndex + "-" + DateUtil.format(
                    flinkTaskAnalysis.getCreateTime(), "yyyy-MM-dd");
            response = BulkApi.bulkJson(elasticClient, reportIndex, reports);
        } catch (Exception e) {
            log.error("failed to save reports:", e);
            return flinkTaskAnalysis;
        }

        BulkItemResponse[] responses = response.getItems();
        for (BulkItemResponse r : responses) {
            if (r.isFailed()) {
                log.info("saveGCReportsErr:", r.getFailure().getCause());
            }
        }
        // 返回数据
        return flinkTaskAnalysis;
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
        FlinkTaskAppExample example = new FlinkTaskAppExample();
        example.createCriteria()
                .andTaskStateEqualTo(FlinkTaskAppState.RUNNING.getDesc());

        long count = flinkTaskAppMapper.countByExample(example);
        int pageSize = 100;
        int pageNum = (int) Math.ceil((double) count / pageSize);
        for (int i = 1; i <= pageNum; i++) {
            PageHelper.startPage(i, pageSize);
            try {
                List<FlinkTaskApp> flinkTaskApps = flinkTaskAppMapper.selectByExample(example);
                for (FlinkTaskApp flinkTaskApp : flinkTaskApps) {
                    diagnosisApp(flinkTaskApp, start, end, from);
                    updateRealtimeTaskAppStatus(flinkTaskApp);
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
    private Boolean checkNeedDiagnosisHourly(FlinkTaskApp job, long start, long end) {
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
        FlinkTaskAppExample example = new FlinkTaskAppExample();
        example.createCriteria()
                .andTaskStateEqualTo(FlinkTaskAppState.RUNNING.getDesc());

        int pageSize = 100;

        long count = flinkTaskAppMapper.countByExample(example);
        int pageNum = (int) Math.ceil((double) count / pageSize);

        for (int i = 1; i <= pageNum; i++) {
            PageHelper.startPage(i, pageSize);
            try {
                List<FlinkTaskApp> flinkTaskApps = flinkTaskAppMapper.selectByExample(example);
                for (FlinkTaskApp flinkTaskApp : flinkTaskApps) {
//                    if (checkNeedDiagnosisHourly(flinkTaskApp, start, end)) {
                    diagnosisApp(flinkTaskApp, start, end, from);
//                    }
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
     * @param flinkTaskApp
     */
    private void fillRcJobDiagnosisWithTaskMeta(RcJobDiagnosis rcJobDiagnosis, FlinkTaskApp flinkTaskApp) {
        rcJobDiagnosis.setJobName(flinkTaskApp.getJobName());
        rcJobDiagnosis.setParallel(flinkTaskApp.getParallel());
        rcJobDiagnosis.setTmSlotNum(flinkTaskApp.getTmSlot());
        if (flinkTaskApp.getParallel() != null && flinkTaskApp.getTmSlot() != null) {
            rcJobDiagnosis.setTmNum((int) Math.ceil((double) flinkTaskApp.getParallel() / flinkTaskApp.getTmSlot()));
        }
        rcJobDiagnosis.setTmMem(flinkTaskApp.getTmMem());
        rcJobDiagnosis.setJmMem(flinkTaskApp.getJmMem());
        rcJobDiagnosis.setTmCore(flinkTaskApp.getTmCore());
    }

    /**
     * 填充元数据
     *
     * @param flinkTaskAnalysis
     * @param flinkTaskApp
     */
    private void fillFlinkTaskAnalysisWithTaskMeta(FlinkTaskAnalysis flinkTaskAnalysis, FlinkTaskApp flinkTaskApp) {
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setUsername(flinkTaskApp.getUsername());
        simpleUser.setUserId(flinkTaskApp.getUserId());

        flinkTaskAnalysis.setFlinkTaskAppId(flinkTaskApp.getId());
        flinkTaskAnalysis.setUsers(Collections.singletonList(simpleUser));
        flinkTaskAnalysis.setProjectName(flinkTaskApp.getProjectName());
        flinkTaskAnalysis.setProjectId(flinkTaskApp.getProjectId());
        flinkTaskAnalysis.setFlowName(flinkTaskApp.getFlowName());
        flinkTaskAnalysis.setFlowId(flinkTaskApp.getFlowId());
        flinkTaskAnalysis.setTaskName(flinkTaskApp.getTaskName());
        flinkTaskAnalysis.setTaskId(flinkTaskApp.getTaskId());
        flinkTaskAnalysis.setApplicationId(flinkTaskApp.getApplicationId());
        flinkTaskAnalysis.setFlinkTrackUrl(flinkTaskApp.getFlinkTrackUrl());
        flinkTaskAnalysis.setAllocatedMB(flinkTaskApp.getAllocatedMb());
        flinkTaskAnalysis.setAllocatedVcores(flinkTaskApp.getAllocatedVcores());
        flinkTaskAnalysis.setRunningContainers(flinkTaskApp.getRunningContainers());
        flinkTaskAnalysis.setEngineType(flinkTaskApp.getEngineType());
        flinkTaskAnalysis.setExecutionDate(flinkTaskApp.getExecutionTime());
        flinkTaskAnalysis.setDuration(flinkTaskApp.getDuration());
        flinkTaskAnalysis.setStartTime(flinkTaskApp.getStartTime());
        flinkTaskAnalysis.setEndTime(flinkTaskApp.getEndTime());
        flinkTaskAnalysis.setVcoreSeconds(flinkTaskApp.getVcoreSeconds());
        flinkTaskAnalysis.setMemorySeconds(flinkTaskApp.getMemorySeconds());
        flinkTaskAnalysis.setQueue(flinkTaskApp.getQueue());
        flinkTaskAnalysis.setClusterName(flinkTaskApp.getClusterName());
        flinkTaskAnalysis.setRetryTimes(flinkTaskApp.getRetryTimes());
        flinkTaskAnalysis.setExecuteUser(flinkTaskApp.getExecuteUser());
        flinkTaskAnalysis.setDiagnosis(flinkTaskApp.getDiagnosis());
        flinkTaskAnalysis.setJobName(flinkTaskApp.getJobName());
    }

    /**
     * 填充上下文诊断数据
     *
     * @param flinkTaskAnalysis
     * @param context
     */
    private void fillFlinkTaskAnalysisWithDiagnosisContext(FlinkTaskAnalysis flinkTaskAnalysis, DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        flinkTaskAnalysis.setParallel(rcJobDiagnosis.getParallel());
        flinkTaskAnalysis.setTmSlot(rcJobDiagnosis.getTmSlotNum());
        flinkTaskAnalysis.setTmCore(rcJobDiagnosis.getTmCore());
        flinkTaskAnalysis.setTmMemory(rcJobDiagnosis.getTmMem());
        flinkTaskAnalysis.setJmMemory(rcJobDiagnosis.getJmMem());
        flinkTaskAnalysis.setTmNum(rcJobDiagnosis.getTmNum());
        flinkTaskAnalysis.setDiagnosisEndTime(new Date(context.getEnd() * 1000));
        flinkTaskAnalysis.setDiagnosisStartTime(new Date(context.getStart() * 1000));
        flinkTaskAnalysis.setDiagnosisSource(context.getFrom().getCode());
        flinkTaskAnalysis.setDiagnosisParallel(rcJobDiagnosis.getDiagnosisParallel());
        flinkTaskAnalysis.setDiagnosisJmMemory(rcJobDiagnosis.getDiagnosisJmMem());
        flinkTaskAnalysis.setDiagnosisTmMemory(rcJobDiagnosis.getDiagnosisTmMem());
        flinkTaskAnalysis.setDiagnosisTmSlotNum(rcJobDiagnosis.getDiagnosisTmSlot());
        flinkTaskAnalysis.setDiagnosisTmCoreNum(rcJobDiagnosis.getDiagnosisTmCore());
        flinkTaskAnalysis.setDiagnosisTmNum(rcJobDiagnosis.getDiagnosisTmNum());
        flinkTaskAnalysis.setCreateTime(new Date());
        flinkTaskAnalysis.setUpdateTime(new Date());
        // 资源调优类型
        if (flinkTaskAnalysis.getTmNum() != null && flinkTaskAnalysis.getTmCore() != null) {
            int preCore = flinkTaskAnalysis.getTmNum() * flinkTaskAnalysis.getTmCore();
            int newCore = flinkTaskAnalysis.getDiagnosisTmNum() * flinkTaskAnalysis.getDiagnosisTmCoreNum();
            // 判断资源变化
            if (newCore > preCore) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.INCR_CPU.getCode());
            } else if (newCore < preCore) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.DECR_CPU.getCode());
            }
        }
        // 资源调优类型
        if (flinkTaskAnalysis.getTmNum() != null && flinkTaskAnalysis.getTmMemory() != null) {
            int preMem = flinkTaskAnalysis.getTmNum() * flinkTaskAnalysis.getTmMemory();
            int newMem = flinkTaskAnalysis.getDiagnosisTmNum() * flinkTaskAnalysis.getDiagnosisTmMemory();
            // 判断资源变化
            if (newMem > preMem) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.INCR_MEM.getCode());
            } else if (newMem < preMem) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.DECR_MEM.getCode());
            }
        }

        // 诊断规则类型
        List<RcJobDiagnosisAdvice> advices = context.getAdvices();
        for (RcJobDiagnosisAdvice rcJobDiagnosisAdvice : advices) {
            if (rcJobDiagnosisAdvice.getHasAdvice()) {
                if (rcJobDiagnosisAdvice.getAdviceType().getCode() == DiagnosisRuleType.RuntimeExceptionRule.getCode()) {
                    addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.RUNTIME_EXCEPTION.getCode());
                }
                addDiagnosisTypes(flinkTaskAnalysis, rcJobDiagnosisAdvice.getAdviceType().getName());
            }
        }
    }
}
