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
import com.oppo.cloud.common.domain.flink.JobManagerConfigItem;
import com.oppo.cloud.common.domain.flink.enums.*;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.common.domain.opensearch.FlinkTaskAdvice;
import com.oppo.cloud.common.domain.opensearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.opensearch.BulkApi;
import com.oppo.cloud.flink.advice.DiagnosisDoctor;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.flink.service.DiagnosisService;
import com.oppo.cloud.flink.service.FlinkOpenSearchService;
import com.oppo.cloud.flink.service.FlinkMetaService;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.Blocklist;
import com.oppo.cloud.model.BlocklistExample;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskAppExample;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.JOB_UP_TIME;

/**
 * Diagnosis Service
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
    private MonitorMetricUtil monitorMetricUtil;

    @Autowired
    private DiagnosisParamsConstants cons;


    @Autowired
    private RestHighLevelClient restClient;

    @Value("${custom.opensearch.flinkReportIndex.name}")
    private String flinkReportIndex;

    @Value("${custom.opensearch.flinkTaskAnalysisIndex.name}")
    private String flinkTaskAnalysisIndex;

    @Autowired
    private FlinkOpenSearchService flinkOpenSearchService;

    /**
     * Add diagnostic type.
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
     * Add diagnostic resource type.
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
        // Check via tracking URL.
        List<JobManagerConfigItem> jobManagerConfigItems = flinkMetaService.reqFlinkConfig(flinkTaskApp.getFlinkTrackUrl());
        if (System.currentTimeMillis() - flinkTaskApp.getCreateTime().getTime() > 1000 * 60 * 30 && jobManagerConfigItems == null) {
            log.info("Failed to access tracking URL for the job: {}", flinkTaskApp);
            flinkTaskApp.setTaskState(FlinkTaskAppState.FINISHED.getDesc());
            flinkTaskAppMapper.updateByPrimaryKey(flinkTaskApp);
            return;
        }

        // Check if the app is in running status.
        String appId = flinkTaskApp.getApplicationId();
        YarnApp app = flinkMetaService.requestYarnApp(appId);
        if (app == null) {
            log.info("App {} not found", appId);
            return;
        }
        // If the status of the app is detected as FINISHED, FAILED, or KILLED, mark the metadata status as finished.
        if (
                app.getState().equalsIgnoreCase(YarnApplicationState.FINISHED.getDesc()) ||
                        app.getState().equalsIgnoreCase(YarnApplicationState.FAILED.getDesc()) ||
                        app.getState().equalsIgnoreCase(YarnApplicationState.KILLED.getDesc())
        ) {
            flinkTaskApp.setTaskState(FlinkTaskAppState.FINISHED.getDesc());
            flinkTaskAppMapper.updateByPrimaryKey(flinkTaskApp);
            log.info(" The app {} has already stopped.", appId);
        }
    }

    /**
     * Diagnostic job.
     *
     * @param flinkTaskApp
     * @param start        seconds
     * @param end          seconds
     * @param from
     * @return
     */
    @Override
    public FlinkTaskAnalysis diagnosisApp(FlinkTaskApp flinkTaskApp, long start, long end,
                                          DiagnosisFrom from) throws Exception {
        // Whitelist check.
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
            log.debug("Whitelist block:{}", flinkTaskApp);
            return null;
        } else {
            log.debug("Whitelist passed:{}", flinkTaskApp);
        }
        RcJobDiagnosis rcJobDiagnosis = new RcJobDiagnosis();
        FlinkTaskAnalysis flinkTaskAnalysis = new FlinkTaskAnalysis();
        flinkTaskAnalysis.setDiagnosisResourceType(new ArrayList<>());

        // 1.Fill metadata into diagnostic result.
        // Get jobName and runtime configuration parameters from Flink UI config.
        // If unable to access, it indicates that there is an issue with the job.
        List<JobManagerConfigItem> flinkConfigItems = flinkMetaService.reqFlinkConfig(flinkTaskApp.getFlinkTrackUrl());
        if (flinkConfigItems == null) {
            log.info("flink ui unable to access. {}", flinkTaskApp.getFlinkTrackUrl());
            return null;
        }
        String jobId = flinkMetaService.getJobId(flinkTaskApp.getFlinkTrackUrl());
        if (jobId == null) {
            log.info("Failed to fetch/get flink jobId, track url: {}", flinkTaskApp.getFlinkTrackUrl());
            return null;
        }
        List<String> tmIds = flinkMetaService.getTmIds(flinkTaskApp.getFlinkTrackUrl());
        // Update the metadata through the configuration of Flink Job Manager.
        flinkMetaService.fillFlinkMetaWithFlinkConfigOnYarn(flinkTaskApp, flinkConfigItems, jobId);
        // Update metadata.
        flinkTaskAppMapper.updateByPrimaryKeySelective(flinkTaskApp);
        // Fill the updated metadata into diagnostic result.
        fillFlinkTaskAnalysisWithTaskMeta(flinkTaskAnalysis, flinkTaskApp);
        // Construct diagnostic context and fill metadata into context.
        fillRcJobDiagnosisWithTaskMeta(rcJobDiagnosis, flinkTaskApp);
        // Construct context.
        DiagnosisContext context = new DiagnosisContext(rcJobDiagnosis, start, end, flinkDiagnosisMetricsServiceImpl, from);
        context.getMessages().put(DiagnosisParam.JobId, jobId);
        context.getMessages().put(DiagnosisParam.TmIds, tmIds);

        // 2.Execute diagnostic phase.
        diagnosisDoctor.diagnosis(context);
        if (context.getStopResourceDiagnosis()) {
            log.info("If the diagnostic conditions are not met, return null.");
            return null;
        }
        // Fill the diagnostic result into FlinkTaskDiagnosis.
        fillFlinkTaskAnalysisWithDiagnosisContext(flinkTaskAnalysis, context);
        // 3.Data storage phase.
        // 3.1 Store FlinkTaskAnalysis.
        // Construct FlinkTaskDiagnosisRuleAdvice and fill in the ID of FlinkTaskDiagnosis.
        List<RcJobDiagnosisAdvice> advices = context.getAdvices();
        List<String> reports = new ArrayList<>();
        List<FlinkTaskAdvice> flinkTaskAdvices = new ArrayList<>();

        String index = flinkTaskAnalysis.genIndex(flinkTaskAnalysisIndex);
        String id = flinkTaskAnalysis.genDocId();

        for (RcJobDiagnosisAdvice advice : advices) {
            FlinkTaskAdvice flinkTaskAdvice = new FlinkTaskAdvice();

            flinkTaskAdvice.setRuleName(advice.getRuleName());
            flinkTaskAdvice.setRuleCode(advice.getAdviceType().getCode());
            flinkTaskAdvice.setRuleAlias(advice.getAdviceType().getZh());
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

        if (resourceTypeSet.contains(2)) { // 2.Reduce CPU.
            int cutCores = totalCores - flinkTaskAnalysis.getDiagnosisTmCoreNum() * flinkTaskAnalysis.getDiagnosisTmNum();
            flinkTaskAnalysis.setCutCoreNum((long) cutCores);
        }

        if (resourceTypeSet.contains(3)) { // 3.Reduce memory.
            int cutMemory = totalMem - flinkTaskAnalysis.getDiagnosisTmMemory() * flinkTaskAnalysis.getDiagnosisTmNum();
            flinkTaskAnalysis.setCutMemNum((long) cutMemory);
        }

        log.debug("diagnosis result: {}", flinkTaskAnalysis);

        UpdateResponse update = flinkOpenSearchService.insertOrUpdate(index, id, flinkTaskAnalysis.genDoc());
        flinkTaskAnalysis.setDocId(id);

        log.debug("update result: {}", update);

        BulkResponse response;
        try {
            String reportIndex = flinkReportIndex + "-" + DateUtil.format(
                    flinkTaskAnalysis.getCreateTime(), "yyyy-MM-dd");
            response = BulkApi.bulkJson(restClient, reportIndex, reports);
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
        // Return data.
        return flinkTaskAnalysis;
    }

    /**
     * Diagnose all jobs.
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
     * Check if the hourly diagnosis needs to be conducted.
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
        // Firstly, if jobUpTimeMetrics returns more than one list, then it is definitely not more than one hour since
        // the job has been online, because hourly diagnosis only diagnoses the data within one hour.
        if (jobUpTimeMetrics == null || jobUpTimeMetrics.size() != 1) {
            log.info("Hourly diagnosis: {} initialization time is null or its size is not equal to 1.", jobUpTime);
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
        // If minUpTime is reported as -1, do not diagnose and fix the reported time correctly.
        if (minUpTime / 1000 / 60 > diagnosisStartMinutes && minUpTime / 1000 / 60 < diagnosisEndMinutes) {
            log.info("Hourly diagnosis:" + metricJobName +
                    String.format("At the beginning of the diagnosis, " +
                                    "the job has been online for %.2f minutes and it is " +
                                    "within the range of %d minutes to %d minutes.", minUpTime / 1000 / 60,
                            diagnosisStartMinutes, diagnosisEndMinutes));
        } else if (minUpTime == -1) {
            log.info(String.format("Hourly diagnosis:%s, all job up time metrics are reported as -1 or 0.", metricJobName));
            return false;
        } else {
            log.info("Hourly diagnosis:" + metricJobName +
                    String.format("The job has been online for %.2f minutes at the beginning of the diagnosis, " +
                            "but it is not within the range of %d minutes to %d minutes, so it cannot be diagnosed.",
                            minUpTime / 1000 / 60, diagnosisStartMinutes, diagnosisEndMinutes));
            return false;
        }
        return true;
    }


    /**
     * Diagnosis starts one hour after the task goes online
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
     * Fill meta
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
     * Fill meta
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
     * Fill context data
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
        // Resource tuning type
        if (flinkTaskAnalysis.getTmNum() != null && flinkTaskAnalysis.getTmCore() != null) {
            int preCore = flinkTaskAnalysis.getTmNum() * flinkTaskAnalysis.getTmCore();
            int newCore = flinkTaskAnalysis.getDiagnosisTmNum() * flinkTaskAnalysis.getDiagnosisTmCoreNum();
            // Judge resource changes
            if (newCore > preCore) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.INCR_CPU.getCode());
            } else if (newCore < preCore) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.DECR_CPU.getCode());
            }
        }
        // Resource tuning type
        if (flinkTaskAnalysis.getTmNum() != null && flinkTaskAnalysis.getTmMemory() != null) {
            int preMem = flinkTaskAnalysis.getTmNum() * flinkTaskAnalysis.getTmMemory();
            int newMem = flinkTaskAnalysis.getDiagnosisTmNum() * flinkTaskAnalysis.getDiagnosisTmMemory();
            // Judge resource changes
            if (newMem > preMem) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.INCR_MEM.getCode());
            } else if (newMem < preMem) {
                addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.DECR_MEM.getCode());
            }
        }

        // Add diagnosis advice
        List<RcJobDiagnosisAdvice> advices = context.getAdvices();
        for (RcJobDiagnosisAdvice rcJobDiagnosisAdvice : advices) {
            if (rcJobDiagnosisAdvice.getHasAdvice()) {
                if (rcJobDiagnosisAdvice.getAdviceType().getCode() == DiagnosisRuleType.RuntimeExceptionRule.getCode()) {
                    addDiagnosisResourceTypes(flinkTaskAnalysis, DiagnosisResourceType.RUNTIME_EXCEPTION.getCode());
                }
                addDiagnosisTypes(flinkTaskAnalysis, rcJobDiagnosisAdvice.getAdviceType().name());
            }
        }
    }
}
