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

package com.oppo.cloud.portal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.ApplicationType;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.JobInstance;
import com.oppo.cloud.common.domain.elasticsearch.SimpleUser;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.MemWasteAbnormal;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.domain.oneclick.OneClickProgress;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.mapper.TaskMapper;
import com.oppo.cloud.mapper.UserMapper;
import com.oppo.cloud.model.*;
import com.oppo.cloud.portal.common.CommonCode;
import com.oppo.cloud.portal.domain.diagnose.DiagnoseReport;
import com.oppo.cloud.portal.domain.diagnose.GCReportResp;
import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.domain.diagnose.oneclick.DiagnoseResult;
import com.oppo.cloud.portal.domain.diagnose.runerror.RunError;
import com.oppo.cloud.portal.domain.task.*;
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.service.JobService;
import com.oppo.cloud.portal.service.TaskAppService;
import com.oppo.cloud.portal.service.diagnose.resource.ResourceBaseService;
import com.oppo.cloud.portal.service.diagnose.runerror.RunErrorBaseService;
import com.oppo.cloud.portal.service.diagnose.runinfo.RunInfoService;
import com.oppo.cloud.portal.service.diagnose.runtime.RunTimeBaseService;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Slf4j
@Service
public class TaskAppServiceImpl implements TaskAppService {

    @Value(value = "${custom.elasticsearch.appIndex.name}")
    private String taskAppsIndex;

    @Value(value = "${custom.elasticsearch.detectIndex.name}")
    private String detectIndex;

    @Value(value = "${custom.elasticsearch.yarnIndex.name}")
    private String yarnAppIndex;

    @Value(value = "${custom.elasticsearch.sparkIndex.name}")
    private String sparkAppIndex;

    @Value("${custom.sparkUiProxy.url}")
    private String sparkUiProxy;

    @Value("${custom.redis.logRecordKey}")
    private String logRecordKey;

    @Autowired
    private JobService jobService;

    @Autowired
    private RunInfoService runInfoService;

    @Autowired
    private List<RunErrorBaseService> runErrorServiceList;

    @Autowired
    private List<RunTimeBaseService> runtimeServiceList;

    @Autowired
    private List<ResourceBaseService> resourceServiceList;

    @Resource(name = "diagnoseExecutor")
    private Executor executor;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TaskApplicationMapper taskApplicationMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * Application列表
     */
    @Override
    public TaskAppsResponse searchTaskApps(TaskAppsRequest request) throws Exception {
        Map<String, Object> termQuery = request.getTermQuery();
        Map<String, SortOrder> sort = request.getSortOrder();
        Map<String, Object[]> rangeConditions = request.getRangeConditions();

        SearchSourceBuilder builder = elasticSearchService.genSearchBuilder(termQuery, rangeConditions, sort, null);
        Long count = elasticSearchService.count(builder, taskAppsIndex + "-*");
        builder.from(request.getFrom()).size(request.getSize());

        List<TaskApp> items = elasticSearchService.find(TaskApp.class, builder, taskAppsIndex + "-*");
        List<TaskAppInfo> appInfoList = items.stream().map(TaskAppInfo::from).collect(Collectors.toList());

        TaskAppsResponse response = new TaskAppsResponse();
        response.setTaskApps(appInfoList);
        response.setCount(count);
        return response;
    }

    /**
     * 生成诊断报
     */
    @Override
    public DiagnoseReport generateReport(String applicationId) throws Exception {
        DiagnoseReport diagnoseReport = new DiagnoseReport();
        List<Item<RunError>> runErrorItemList = diagnoseReport.getRunErrorAnalyze();
        List<Item> runTimeAnalyze = diagnoseReport.getRunTimeAnalyze();
        List<Item> resourceAnalyze = diagnoseReport.getResourcesAnalyze();
        DetectorStorage detectorStorage;
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", applicationId);
        List<DetectorStorage> detectorStorageList =
                elasticSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
        if (detectorStorageList.size() == 0) {
            detectorStorage = new DetectorStorage();
            detectorStorage.setApplicationId(applicationId);
        } else {
            detectorStorage = detectorStorageList.get(0);
        }
        // 生成运行信息
        DetectorStorage finalDetectorStorage = detectorStorage;
        CompletableFuture<DiagnoseReport.RunInfo> runInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            DiagnoseReport.RunInfo runInfo = runInfoService.generateRunInfo(finalDetectorStorage);
            long endTime = System.currentTimeMillis();
            log.info("{} finished，duration:{}", runInfoService.getClass().getName(), (endTime - startTime) / 1000);
            return runInfo;
        }, executor);
        // 生成运行错误类型报告
        List<CompletableFuture<Item<RunError>>> runErrorItemCompletableFutureList = new ArrayList<>();
        for (RunErrorBaseService runErrorBaseService : runErrorServiceList) {
            runErrorItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item<RunError> runErrorItem = runErrorBaseService.generate(finalDetectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finished，duration:{}", runErrorBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return runErrorItem;
            }, executor));
        }
        // 生成运行耗时类型报告
        List<CompletableFuture<Item>> runTimeItemCompletableFutureList = new ArrayList<>();
        for (RunTimeBaseService runTimeBaseService : runtimeServiceList) {
            runTimeItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item item = runTimeBaseService.generate(finalDetectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finished，duration:{}", runTimeBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return item;
            }, executor));
        }
        // 生成资源类型报告
        List<CompletableFuture<Item>> resourceItemCompletableFutureList = new ArrayList<>();
        for (ResourceBaseService resourceBaseService : resourceServiceList) {
            resourceItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item item = resourceBaseService.generate(finalDetectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finish，duration:{}", resourceBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return item;
            }, executor));
        }
        // 获取异步结果
        diagnoseReport.setRunInfo(runInfoCompletableFuture.get());
        for (CompletableFuture<Item<RunError>> completableFuture : runErrorItemCompletableFutureList) {
            runErrorItemList.add(completableFuture.get());
        }
        for (CompletableFuture<Item> completableFuture : runTimeItemCompletableFutureList) {
            runTimeAnalyze.add(completableFuture.get());
        }
        for (CompletableFuture<Item> completableFuture : resourceItemCompletableFutureList) {
            resourceAnalyze.add(completableFuture.get());
        }
        runTimeAnalyze.sort((o1, o2) -> {
            if (o1.getItem() != null && o2.getItem() != null) {
                if (o1.getItem() instanceof IsAbnormal && o2.getItem() instanceof IsAbnormal) {
                    if (((IsAbnormal) o1.getItem()).getAbnormal() && !((IsAbnormal) o2.getItem()).getAbnormal()) {
                        // 结果为-1则o1往前排
                        return -1;
                    } else if (o1.getItem() != null) {
                        if (((IsAbnormal) o1.getItem()).getAbnormal()) {
                            return -1;
                        }
                    }
                }
            } else if (o1.getItem() != null) {
                if (((IsAbnormal) o1.getItem()).getAbnormal()) {
                    return -1;
                }
            }
            return 0;
        });
        return diagnoseReport;
    }

    @Override
    public DiagnoseReport.RunInfo diagnoseRunInfo(String applicationId) throws Exception {
        DetectorStorage detectorStorage;
        String taskAppTempKey = applicationId + CommonCode.DIAGNOSE_DETECTORSTORAGE;
        if (redisService.hasKey(taskAppTempKey)) {
            detectorStorage = JSONObject.parseObject((String) redisService.get(taskAppTempKey), DetectorStorage.class);
        } else {
            Map<String, Object> termQuery = new HashMap<>();
            termQuery.put("applicationId.keyword", applicationId);
            List<DetectorStorage> detectorStorageList =
                    elasticSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
            if (detectorStorageList.size() == 0) {
                detectorStorage = new DetectorStorage();
                detectorStorage.setApplicationId(applicationId);
            } else {
                detectorStorage = detectorStorageList.get(0);
                redisService.set(taskAppTempKey, JSONObject.toJSONString(detectorStorage), 24 * 3600L);
            }
        }
        return runInfoService.generateRunInfo(detectorStorage);
    }

    @Override
    public List<Item<RunError>> diagnoseRunError(String applicationId) throws Exception {
        List<Item<RunError>> runErrorItemList = new ArrayList<>();
        String taskAppTempKey = applicationId + CommonCode.DIAGNOSE_RUNERROR;
        if (redisService.hasKey(taskAppTempKey)) {
            runErrorItemList = JSONObject.parseObject((String) redisService.get(taskAppTempKey),
                    new TypeReference<List<Item<RunError>>>() {
                    });
        } else {
            // 生成运行错误类型报告
            List<CompletableFuture<Item<RunError>>> runErrorItemCompletableFutureList = new ArrayList<>();
            for (RunErrorBaseService runErrorBaseService : runErrorServiceList) {
                runErrorItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                    long startTime = System.currentTimeMillis();
                    Item<RunError> runErrorItem = runErrorBaseService.generate(applicationId);
                    long endTime = System.currentTimeMillis();
                    log.info("{} finished，duration:{}", runErrorBaseService.getClass().getName(),
                            (endTime - startTime) / 1000);
                    return runErrorItem;
                }, executor));
            }
            for (CompletableFuture<Item<RunError>> completableFuture : runErrorItemCompletableFutureList) {
                runErrorItemList.add(completableFuture.get());
            }
            redisService.set(taskAppTempKey, JSONObject.toJSONString(runErrorItemList), 24 * 3600L);
        }
        return runErrorItemList;
    }

    @Override
    public List<Item> diagnoseRunTime(String applicationId) throws Exception {
        List<Item> runTimeAnalyze = new ArrayList<>();
        DetectorStorage detectorStorage;
        String taskAppTempKey = applicationId + CommonCode.DIAGNOSE_DETECTORSTORAGE;
        if (redisService.hasKey(taskAppTempKey)) {
            detectorStorage = JSONObject.parseObject((String) redisService.get(taskAppTempKey), DetectorStorage.class);
        } else {
            Map<String, Object> termQuery = new HashMap<>();
            termQuery.put("applicationId.keyword", applicationId);
            List<DetectorStorage> detectorStorageList =
                    elasticSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
            if (detectorStorageList.size() == 0) {
                detectorStorage = new DetectorStorage();
                detectorStorage.setApplicationId(applicationId);
            } else {
                detectorStorage = detectorStorageList.get(0);
                redisService.set(taskAppTempKey, JSONObject.toJSONString(detectorStorage), 24 * 3600L);
            }
        }
        // 生成运行耗时类型报告
        List<CompletableFuture<Item>> runTimeItemCompletableFutureList = new ArrayList<>();
        for (RunTimeBaseService runTimeBaseService : runtimeServiceList) {
            runTimeItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item item = runTimeBaseService.generate(detectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finished，duration:{}", runTimeBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return item;
            }, executor));
        }
        for (CompletableFuture<Item> completableFuture : runTimeItemCompletableFutureList) {
            runTimeAnalyze.add(completableFuture.get());
        }
        runTimeAnalyze.sort((o1, o2) -> {
            if (o1.getItem() != null && o2.getItem() != null) {
                if (o1.getItem() instanceof IsAbnormal && o2.getItem() instanceof IsAbnormal) {
                    if (((IsAbnormal) o1.getItem()).getAbnormal() && !((IsAbnormal) o2.getItem()).getAbnormal()) {
                        // 结果为-1则o1往前排
                        return -1;
                    } else if (o1.getItem() != null) {
                        if (((IsAbnormal) o1.getItem()).getAbnormal()) {
                            return -1;
                        }
                    }
                }
            } else if (o1.getItem() != null) {
                if (((IsAbnormal) o1.getItem()).getAbnormal()) {
                    return -1;
                }
            }
            return 0;
        });
        return runTimeAnalyze;
    }

    @Override
    public List<Item> diagnoseRunResource(String applicationId) throws Exception {
        List<Item> resourceAnalyze = new ArrayList<>();
        DetectorStorage detectorStorage;
        String taskAppTempKey = applicationId + CommonCode.DIAGNOSE_DETECTORSTORAGE;
        if (redisService.hasKey(taskAppTempKey)) {
            detectorStorage = JSONObject.parseObject((String) redisService.get(taskAppTempKey), DetectorStorage.class);
        } else {
            Map<String, Object> termQuery = new HashMap<>();
            termQuery.put("applicationId.keyword", applicationId);
            List<DetectorStorage> detectorStorageList =
                    elasticSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
            if (detectorStorageList.size() == 0) {
                detectorStorage = new DetectorStorage();
                detectorStorage.setApplicationId(applicationId);
            } else {
                detectorStorage = detectorStorageList.get(0);
                redisService.set(taskAppTempKey, JSONObject.toJSONString(detectorStorage), 24 * 3600L);
            }
        }
        // 生成资源类型报告
        List<CompletableFuture<Item>> resourceItemCompletableFutureList = new ArrayList<>();
        for (ResourceBaseService resourceBaseService : resourceServiceList) {
            resourceItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item item = resourceBaseService.generate(detectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finish，duration:{}", resourceBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return item;
            }, executor));
        }
        for (CompletableFuture<Item> completableFuture : resourceItemCompletableFutureList) {
            resourceAnalyze.add(completableFuture.get());
        }
        return resourceAnalyze;
    }

    /**
     * job trend graph
     */
    @Override
    public TrendGraph getGraph(JobsRequest request) throws Exception {
        Map<String, Object> termQuery = request.getTermQuery();
        termQuery.put("diagnoseResult", "abnormal");
        Map<String, Object[]> rangeConditions = request.getRangeConditions();
        SearchSourceBuilder builder = elasticSearchService.genSearchBuilder(termQuery, rangeConditions, null, null);

        TrendGraph trendGraph = new TrendGraph();
        List<IndicatorData> data = null;
        double maxValue;
        String timeUnit;
        switch (request.getGraphType()) {
            case "cpuTrend":
                trendGraph.setName("CPU趋势");
                data = elasticSearchService.sumAggregationByDay(builder, request.getStart(), request.getEnd(),
                        taskAppsIndex, "vcoreSeconds");
                break;
            case "memoryTrend":
                trendGraph.setName("内存趋势");
                data = elasticSearchService.sumAggregationByDay(builder, request.getStart(), request.getEnd(),
                        taskAppsIndex, "memorySeconds");
                break;
            case "numTrend":
                trendGraph.setName("数量趋势");
                data = elasticSearchService.countDocByDay(builder, request.getStart(), request.getEnd(), taskAppsIndex);
                break;
            default:
                break;
        }
        if (data == null || data.size() == 0) {
            return null;
        }

        IndicatorData indicatorData = data.stream().max(comparing(IndicatorData::getCount)).orElse(null);
        maxValue = indicatorData.getCount();

        switch (request.getGraphType()) {
            case "cpuTrend":
                timeUnit = UnitUtil.getTimeUnit(maxValue);
                data.forEach(d -> d.setCount(UnitUtil.convertCpuUnit(timeUnit, d.getCount())));
                trendGraph.setUnit(String.format("vcore·%s", timeUnit));
                break;
            case "memoryTrend":
                timeUnit = UnitUtil.getTimeUnit(maxValue / 1024);
                data.forEach(d -> d.setCount(UnitUtil.convertMemoryUnit(timeUnit, d.getCount())));
                trendGraph.setUnit(String.format("GB·%s", timeUnit));
                break;
            default:
                break;
        }

        trendGraph.setData(data);
        return trendGraph;
    }

    @Override
    public List<Item> generatePartOfReport(String applicationId, Set<String> category) throws Exception {
        List<Item> res = new ArrayList<>();
        DetectorStorage detectorStorage;
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", applicationId);
        List<DetectorStorage> detectorStorageList =
                elasticSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
        if (detectorStorageList.size() == 0) {
            detectorStorage = new DetectorStorage();
            detectorStorage.setApplicationId(applicationId);
        } else {
            detectorStorage = detectorStorageList.get(0);
        }
        DetectorStorage finalDetectorStorage = detectorStorage;
        // 生成运行错误类型报告
        List<CompletableFuture<Item<RunError>>> runErrorItemCompletableFutureList = new ArrayList<>();
        for (RunErrorBaseService runErrorBaseService : runErrorServiceList) {
            // 只生成目标异常类型的诊断报告
            if (!category.contains(runErrorBaseService.getCategory())) {
                continue;
            }
            runErrorItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item<RunError> runErrorItem = runErrorBaseService.generate(finalDetectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finished，duration:{}", runErrorBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return runErrorItem;
            }, executor));
        }
        // 生成运行耗时类型报告
        List<CompletableFuture<Item>> runTimeItemCompletableFutureList = new ArrayList<>();
        for (RunTimeBaseService runTimeBaseService : runtimeServiceList) {
            // 只生成目标异常类型的诊断报告
            if (!category.contains(runTimeBaseService.getCategory())) {
                continue;
            }
            runTimeItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item item = runTimeBaseService.generate(finalDetectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finished，duration:{}", runTimeBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return item;
            }, executor));
        }
        // 生成资源类型报告
        List<CompletableFuture<Item>> resourceItemCompletableFutureList = new ArrayList<>();
        for (ResourceBaseService resourceBaseService : resourceServiceList) {
            if (!category.contains(resourceBaseService.getCategory())) {
                continue;
            }
            resourceItemCompletableFutureList.add(CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                Item item = resourceBaseService.generate(finalDetectorStorage);
                long endTime = System.currentTimeMillis();
                log.info("{} finish，duration:{}", resourceBaseService.getClass().getName(),
                        (endTime - startTime) / 1000);
                return item;
            }, executor));
        }

        for (CompletableFuture<Item<RunError>> completableFuture : runErrorItemCompletableFutureList) {
            res.add(completableFuture.get());
        }
        for (CompletableFuture<Item> completableFuture : runTimeItemCompletableFutureList) {
            res.add(completableFuture.get());
        }
        for (CompletableFuture<Item> completableFuture : resourceItemCompletableFutureList) {
            res.add(completableFuture.get());
        }
        return res;
    }

    @Override
    public GCReportResp getGcReport(String applicationId, String executorId) throws Exception {
        GCReportResp gcReportResp = new GCReportResp();
        DetectorStorage detectorStorage = new DetectorStorage();
        String taskAppTempKey = applicationId + CommonCode.DIAGNOSE_DETECTORSTORAGE;
        if (redisService.hasKey(taskAppTempKey)) {
            detectorStorage = JSONObject.parseObject((String) redisService.get(taskAppTempKey), DetectorStorage.class);
        } else {
            Map<String, Object> termQuery = new HashMap<>();
            termQuery.put("applicationId.keyword", applicationId);
            List<DetectorStorage> detectorStorageList =
                    elasticSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
            if (detectorStorageList.size() > 0) {
                detectorStorage = detectorStorageList.get(0);
            }
        }
        MemWasteAbnormal memWasteAbnormal = null;
        for (DetectorResult detectorResult : detectorStorage.getDataList()) {
            if (detectorResult.getAppCategory().equals(AppCategoryEnum.MEMORY_WASTE.getCategory())) {
                memWasteAbnormal = ((JSONObject) detectorResult.getData()).toJavaObject(MemWasteAbnormal.class);
            }
        }
        if (memWasteAbnormal == null || memWasteAbnormal.getGcReportList().size() == 0) {
            return gcReportResp;
        }
        List<GCReport> gcReportList = memWasteAbnormal.getGcReportList();
        for (GCReport gcReport : gcReportList) {
            if (gcReport.getExecutorId().equals(Integer.valueOf(executorId))) {
                gcReportResp.build(gcReport);
                return gcReportResp;
            }
        }
        return gcReportResp;
    }

    @Override
    public DiagnoseResult diagnose(String applicationId) throws Exception {
        // 查询是否已经在检测中，或者检测完成
        DiagnoseResult diagnoseResult = findRedis(applicationId);
        if (diagnoseResult != null) {
            return diagnoseResult;
        }
        // 从已检测库中直接查(es中有记录且es中记录的categories不为空则直接返回结果)
        diagnoseResult = findEs(applicationId);
        if (diagnoseResult != null) {
            return diagnoseResult;
        }
        // 发起检测流程(今天没有检测过，es中没有记录，es中记录的categories为空)
        diagnoseResult = sponsorDiagnose(applicationId);
        return diagnoseResult;
    }

    /**
     * 判断是否已经检测过，直接从ES数据库查询
     */
    private DiagnoseResult findEs(String applicationId) throws Exception {
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", applicationId);
        List<TaskApp> taskAppList = elasticSearchService.find(TaskApp.class, termQuery, taskAppsIndex + "-*");
        if (taskAppList.size() != 0) {
            TaskApp taskApp = taskAppList.get(0);
            // categories为空则进行诊断
            if (taskApp.getCategories() == null || taskApp.getCategories().size() == 0) {
                return null;
            }
            if (taskApp.getCategories().size() == 1
                    && taskApp.getCategories().contains(AppCategoryEnum.OTHER_EXCEPTION.getCategory())
                    && taskApp.getDiagnostics() != null) {
                // 只有“其他异常”类型则进行检测
                return null;
            }
            TaskAppInfo taskAppInfo = TaskAppInfo.from(taskApp);
            DiagnoseResult diagnoseResult = new DiagnoseResult();
            diagnoseResult.setStatus("succeed");
            diagnoseResult.setTaskAppInfo(taskAppInfo);
            diagnoseResult.getProcessInfoList().add(new DiagnoseResult.ProcessInfo("该ApplicationId已经诊断完成", 100));
            return diagnoseResult;
        }
        // Es中找不到则进行诊断
        return null;
    }

    /**
     * 判断是否在检测中，从redis中查询
     */
    private DiagnoseResult findRedis(String applicationId) throws Exception {
        String taskAppTempKey = applicationId + CommonCode.TASK_APP_TEMP;
        if (redisService.hasKey(taskAppTempKey)) {
            DiagnoseResult diagnoseResult = new DiagnoseResult();
            TaskApp taskApp = new TaskApp();
            TaskAppInfo taskAppInfo = new TaskAppInfo();
            List<DiagnoseResult.ProcessInfo> processInfoList = new ArrayList<>();
            String eventLogStatus = this.checkEventLogProcess(applicationId, processInfoList);
            String executorLogStatus = this.checkExecutorLogProcess(applicationId, processInfoList);
            // 获取暂存的TaskApp
            if (redisService.hasKey(taskAppTempKey)) {
                String taskAppTempStr = (String) redisService.get(taskAppTempKey);
                taskApp = JSON.parseObject(taskAppTempStr, TaskApp.class);
            }
            log.info("开始检测event 和driver 的状态:{},{}", eventLogStatus, executorLogStatus);
            if (!"processing".equals(eventLogStatus) && !"processing".equals(executorLogStatus)) {
                // 诊断已完成
                if ("failed".equals(eventLogStatus) && "failed".equals(executorLogStatus)) {
                    // 诊断失败
                    diagnoseResult.setStatus("failed");
                    diagnoseResult.setTaskAppInfo(null);
                    diagnoseResult.setErrorMsg("");
                } else {
                    // 诊断成功
                    diagnoseResult.setStatus("succeed");
                    // 查询Es数据
                    HashMap<String, Object> termQuery = new HashMap<>();
                    termQuery.put("applicationId.keyword", applicationId);
                    List<TaskApp> taskAppList =
                            elasticSearchService.find(TaskApp.class, termQuery, taskAppsIndex + "-*");
                    if (taskAppList.size() != 0) {
                        TaskApp taskAppEs = taskAppList.get(0);
                        taskAppInfo = TaskAppInfo.from(taskAppEs);
                    } else {
                        taskAppInfo = TaskAppInfo.from(taskApp);
                    }
                }
            } else {
                // 诊断中
                diagnoseResult.setStatus("processing");
                taskAppInfo = TaskAppInfo.from(taskApp);
                // 未诊断结束的不显示
                taskAppInfo.setCategories(new ArrayList<>());
            }
            log.info("判断状态结束");
            diagnoseResult.setTaskAppInfo(taskAppInfo);
            diagnoseResult.setProcessInfoList(processInfoList);
            return diagnoseResult;
        }
        return null;
    }

    /**
     * 检测EventLog的检测进度
     */
    private String checkEventLogProcess(String applicationId,
                                        List<DiagnoseResult.ProcessInfo> processInfoList) throws Exception {
        String eventDetectKey = applicationId + CommonCode.EVENT_DETECT;
        OneClickProgress oneClickProgress;
        String eventLogDetectStatus = "";
        Integer eventLogSize = 0;
        String eventLogStatus = "processing";
        // event诊断状态
        if (redisService.hasKey(eventDetectKey)) {
            String eventLogDetectStr = (String) redisService.get(eventDetectKey);
            oneClickProgress = JSON.parseObject(eventLogDetectStr, OneClickProgress.class);
            eventLogDetectStatus = oneClickProgress.getProgressInfo().getState().name().toLowerCase();
            eventLogSize = oneClickProgress.getProgressInfo().getCount();
        } else {
            eventLogDetectStatus = "start";
        }
        switch (eventLogDetectStatus) {
            case "succeed":
                processInfoList.add(new DiagnoseResult.ProcessInfo("Event Log 诊断完成", 100));
                eventLogStatus = "succeed";
                break;
            case "failed":
                processInfoList.add(new DiagnoseResult.ProcessInfo("Event Log 诊断失败, 请联系系统管理员", 100));
                eventLogStatus = "failed";
                break;
            case "start":
                processInfoList.add(new DiagnoseResult.ProcessInfo("Event Log 开始诊断", 10));
                break;
            default:
                processInfoList.add(new DiagnoseResult.ProcessInfo(String.format("Event Log 诊断中, 文件大小:%s ",
                        UnitUtil.transferByte(eventLogSize == null ? 0 : eventLogSize)), 50));
        }
        return eventLogStatus;
    }

    /**
     * 检测Executor的检测进度
     */
    private String checkExecutorLogProcess(String applicationId,
                                           List<DiagnoseResult.ProcessInfo> processInfoList) throws Exception {
        String executorParseKey = applicationId + CommonCode.EXECUTOR_PARSER;
        OneClickProgress executorParse;
        String executorLogStatus = "processing";
        if (redisService.hasKey(executorParseKey)) {
            String executorParseStr = (String) redisService.get(executorParseKey);
            executorParse = JSON.parseObject(executorParseStr, OneClickProgress.class);
            Integer count = executorParse.getProgressInfo().getCount();
            Integer process = executorParse.getProgressInfo().getProgress();
            switch (executorParse.getProgressInfo().getState().name().toLowerCase()) {
                case "processing":
                    processInfoList.add(new DiagnoseResult.ProcessInfo(
                            String.format("Driver/Executor Log 诊断中, 文件总数:%d, 已解析文件数:%d",
                                    count, process),
                            100 * Double.parseDouble(String.format("%.2f", process / (double) count))));
                    break;
                case "succeed":
                    processInfoList.add(new DiagnoseResult.ProcessInfo("Driver/Executor Log 诊断完成", 100));
                    executorLogStatus = "succeed";
                    break;
                case "failed":
                    processInfoList.add(new DiagnoseResult.ProcessInfo("Driver/Executor Log 诊断失败, 请联系系统管理员", 100));
                    executorLogStatus = "failed";
                    log.info("executor失败：{}", executorParseStr);
                    break;
                default:
            }
        } else {
            processInfoList.add(new DiagnoseResult.ProcessInfo("Driver/Executor Log 开始诊断", 10));
        }
        return executorLogStatus;
    }

    /**
     * 未检测也未提交检测
     */
    private DiagnoseResult sponsorDiagnose(String applicationId) throws Exception {
        TaskApp taskApp = this.buildTaskApp(applicationId);
        JobInstance jobInstance;
        if (!taskApp.getApplicationType().equals(ApplicationType.SPARK.getValue())) {
            throw new Exception(String.format("暂不支持%s类型的任务", taskApp.getApplicationType()));
        }
        JobAnalysis jobAnalysis = new JobAnalysis();
        jobInstance = jobService.getJobInstance(taskApp.getProjectName(),
                taskApp.getFlowName(), taskApp.getTaskName(), taskApp.getExecutionDate());
        BeanUtils.copyProperties(jobInstance, jobAnalysis);
        LogRecord logRecord = new LogRecord();
        logRecord.setJobAnalysis(jobAnalysis);
        App app = new App();
        app.formatAppLog(taskApp);
        logRecord.setApps(Collections.singletonList(app));
        logRecord.formatTaskAppList(Collections.singletonList(taskApp));
        // 先把数据存入到Es
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", taskApp.getApplicationId());
        List<TaskApp> taskAppEsList = elasticSearchService.find(TaskApp.class, termQuery, taskAppsIndex + "-*");
        if (taskAppEsList.size() == 0) {
            List<String> categories = new ArrayList<>();
            if (StringUtils.isNotEmpty(taskApp.getDiagnostics())) {
                categories.add(AppCategoryEnum.OTHER_EXCEPTION.getCategory());
            }
            taskApp.setCategories(categories);
            elasticSearchService.insertOrUpDateEs(taskApp.genIndex(taskAppsIndex), taskApp.genDocId(),
                    taskApp.genDoc());
            log.info("写入Es成功");
        }
        // 往右边插入数据,保证优先被解析到
        logRecord.setIsOneClick(true);
        logRecord.setId(UUID.randomUUID().toString());
        String logRecordJson = JSONObject.toJSONString(logRecord);
        Long size = redisService.lRightPush(logRecordKey, logRecordJson);
        log.info("send key:{},size:{},logRecord:{}", logRecordKey, size, logRecordJson);
        // 暂存到redis中
        String taskAppStr = JSON.toJSONString(taskApp);
        String taskAppTempKey = taskApp.getApplicationId() + CommonCode.TASK_APP_TEMP;
        redisService.set(taskAppTempKey, taskAppStr, 3600 * 24);
        // 构造返回给前端的数据结构
        TaskAppInfo taskAppInfo = TaskAppInfo.from(taskApp);
        // 未诊断结束的不显示
        taskAppInfo.setCategories(new ArrayList<>());
        DiagnoseResult diagnoseResult = new DiagnoseResult();
        diagnoseResult.setTaskAppInfo(taskAppInfo);
        diagnoseResult.setStatus("processing");
        List<DiagnoseResult.ProcessInfo> processInfoList = new ArrayList<>();
        processInfoList.add(new DiagnoseResult.ProcessInfo("Event Log 发送诊断成功, 请稍后", 0));
        processInfoList.add(new DiagnoseResult.ProcessInfo("Driver/Executor Log 发送诊断成功, 请稍后", 0));
        diagnoseResult.setProcessInfoList(processInfoList);
        return diagnoseResult;
    }

    /**
     * 根据appId构造TaskApp信息
     */
    private TaskApp buildTaskApp(String applicationId) throws Exception {
        // 获取taskId,dagId,executionDate,try_number,appId信息
        TaskApplication taskApplication;
        YarnApp yarnApp;
        SparkApp sparkApp;
        TaskApplicationExample taskApplicationExample = new TaskApplicationExample();
        taskApplicationExample.createCriteria().andApplicationIdEqualTo(applicationId);
        List<TaskApplication> taskApplicationList = taskApplicationMapper.selectByExample(taskApplicationExample);
        if (taskApplicationList.size() == 0) {
            log.error("can not find this applicationId from task-application, appId:{}", applicationId);
            throw new Exception(String.format("can not find this applicationId from db, appId:%s", applicationId));
        }
        taskApplication = taskApplicationList.get(0);
        HashMap<String, Object> termQueryYarn = new HashMap<>();
        termQueryYarn.put("id.keyword", taskApplication.getApplicationId());
        List<YarnApp> yarnAppList = elasticSearchService.find(YarnApp.class, termQueryYarn, yarnAppIndex + "-*");
        if (yarnAppList.size() == 0) {
            throw new Exception(String.format("can not find this applicationId from yarn-app, appId:%s",
                    taskApplication.getApplicationId()));
        }
        yarnApp = yarnAppList.get(0);
        HashMap<String, Object> termQuerySpark = new HashMap<>();
        termQuerySpark.put("appId.keyword", taskApplication.getApplicationId());
        List<SparkApp> sparkAppList = elasticSearchService.find(SparkApp.class, termQuerySpark, sparkAppIndex + "-*");
        if (sparkAppList.size() == 0) {
            throw new Exception(String.format("can not find this applicationId from spark-app, appId:%s",
                    taskApplication.getApplicationId()));
        }
        sparkApp = sparkAppList.get(0);
        TaskApp taskApp = this.buildAbnormalTaskApp(taskApplication, yarnApp, sparkApp);
        updateUserInfo(taskApp);
        return taskApp;
    }

    /**
     * 根据基础的appId信息构建出AbnormalTaskApp,有异常则直接退出抛出异常
     */
    public TaskApp buildAbnormalTaskApp(TaskApplication taskApplication, YarnApp yarnApp,
                                        SparkApp sparkApp) throws Exception {
        TaskApp taskApp = new TaskApp();
        BeanUtils.copyProperties(taskApplication, taskApp);
        taskApp.setExecutionDate(taskApplication.getExecuteTime());
        taskApp.setStartTime(new Date(yarnApp.getStartedTime()));
        taskApp.setFinishTime(new Date(yarnApp.getFinishedTime()));
        taskApp.setElapsedTime((double) yarnApp.getElapsedTime());
        taskApp.setClusterName(yarnApp.getClusterName());
        taskApp.setApplicationType(yarnApp.getApplicationType());
        taskApp.setQueue(yarnApp.getQueue());
        taskApp.setDiagnostics(yarnApp.getDiagnostics());
        taskApp.setDiagnoseResult(StringUtils.isNotBlank(yarnApp.getDiagnostics()) ? "abnormal" : "");
        taskApp.setCategories(StringUtils.isNotBlank(yarnApp.getDiagnostics())
                ? Collections.singletonList(AppCategoryEnum.OTHER_EXCEPTION.getCategory())
                : new ArrayList<>());
        taskApp.setExecuteUser(yarnApp.getUser());
        taskApp.setVcoreSeconds((double) yarnApp.getVcoreSeconds());
        taskApp.setTaskAppState(yarnApp.getState());
        taskApp.setRetryTimes(taskApplication.getRetryTimes());
        // 单位转换保留两位小数
        taskApp.setMemorySeconds((double) Math.round(yarnApp.getMemorySeconds()));
        String attemptId = StringUtils.isNotEmpty(sparkApp.getAttemptId()) ? sparkApp.getAttemptId() : "1";
        taskApp.setEventLogPath(
                sparkApp.getEventLogDirectory() + "/" + taskApplication.getApplicationId() + "_" + attemptId);
        taskApp.setSparkUI(
                String.format(sparkUiProxy, taskApplication.getApplicationId(), sparkApp.getSparkHistoryServer()));
        String yarnLogPath = getYarnLogPath(yarnApp.getIp());
        if ("".equals(yarnLogPath)) {
            throw new Exception(String.format("can not find yarn log path: rm ip : %s", yarnApp.getIp()));
        }
        // eg: yarnApp.getAmHostHttpAddress() -> ops-metastore-20211108211244-emc6:8042
        String[] amHost = yarnApp.getAmHostHttpAddress().split(":");
        if (amHost.length == 0) {
            throw new Exception(String.format("parse amHost error, amHost:%s", yarnApp.getAmHostHttpAddress()));
        }
        // eg :/tmp/agg_logs_sec/hdfs/logs/application_1642582961937_0437/ops-metastore-20211108211244-emc6_8041
        taskApp.setAmHost(amHost[0]);
        taskApp.setYarnLogPath(yarnLogPath + "/" + yarnApp.getUser() + "/logs/" + taskApplication.getApplicationId());
        return taskApp;
    }

    /**
     * 补充任务的用户信息
     */
    public void updateUserInfo(TaskApp taskApp) {
        TaskExample taskExample = new TaskExample();
        taskExample.createCriteria().andTaskNameEqualTo(taskApp.getTaskName())
                .andFlowNameEqualTo(taskApp.getFlowName())
                .andProjectNameEqualTo(taskApp.getProjectName());
        List<Task> tasks = taskMapper.selectByExample(taskExample);
        if (tasks.size() == 0) {
            log.warn("cant found task: {},{},{}", taskApp.getProjectName(), taskApp.getFlowName(), taskApp.getTaskName());
            return;
        }
        Task task = tasks.get(0);
        if (task != null) {
            taskApp.setTaskId(task.getId());
            taskApp.setProjectId(task.getProjectId());
            taskApp.setFlowId(task.getFlowId());
            UserExample userExample = new UserExample();
            userExample.createCriteria().andUserIdEqualTo(task.getUserId());
            List<User> users = userMapper.selectByExample(userExample);
            if (users.size() > 0) {
                User user = users.get(0);
                SimpleUser esSimpleUser = new SimpleUser();
                esSimpleUser.setUserId(user.getUserId());
                esSimpleUser.setUsername(user.getUsername());
                taskApp.setUsers(Collections.singletonList(esSimpleUser));
            }
        }
    }

    /**
     * 查询redis,获取yarn 日志路径
     */
    public String getYarnLogPath(String rmIp) throws Exception {
        if (redisService.hasKey(Constant.RM_JHS_MAP)) {
            Map<String, List<String>> rmJhsMap = JSON.parseObject((String) redisService.get(Constant.RM_JHS_MAP),
                    new TypeReference<Map<String, List<String>>>() {
                    });
            List<String> jhsIps = rmJhsMap.get(rmIp);
            for (String jhsIp : jhsIps) {
                String key = Constant.JHS_HDFS_PATH + jhsIp;
                if (redisService.hasKey(key)) {
                    return (String) redisService.get(key);
                } else {
                    throw new Exception(String.format(
                            "search redis error,msg: can not find key %s, rmJhsMap:%s, rmIp:%s", key, rmJhsMap, rmIp));
                }
            }
        } else {
            throw new Exception(String.format("search redis error,msg: can not find key %s", Constant.RM_JHS_MAP));
        }
        return "";
    }

}
