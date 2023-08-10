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

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.MemWasteAbnormal;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.mapper.TaskMapper;
import com.oppo.cloud.mapper.UserMapper;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

}
