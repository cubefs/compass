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

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.common.domain.opensearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.domain.flink.*;
import com.oppo.cloud.portal.domain.report.DistributionData;
import com.oppo.cloud.portal.domain.report.DistributionGraph;
import com.oppo.cloud.portal.domain.report.LineGraph;
import com.oppo.cloud.portal.domain.report.TrendGraph;
import com.oppo.cloud.portal.domain.statistics.FlinkStatisticsData;
import com.oppo.cloud.portal.domain.statistics.PeriodTime;
import com.oppo.cloud.portal.domain.task.IndicatorData;
import com.oppo.cloud.portal.domain.task.UserInfo;
import com.oppo.cloud.portal.service.OpenSearchService;
import com.oppo.cloud.portal.service.FlinkTaskDiagnosisService;
import com.oppo.cloud.portal.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.aggregations.metrics.ParsedSum;
import org.opensearch.search.aggregations.metrics.SumAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;


/**
 * 实时任务查询接口
 */
@Service
@Slf4j
public class FlinkTaskDiagnosisServiceImpl implements FlinkTaskDiagnosisService {

    @Autowired
    OpenSearchService openSearchService;

    @Autowired
    HttpUtil httpUtil;

    @Value(value = "${custom.opensearch.flinkReportIndex.name}")
    private String flinkReportIndex;

    @Value(value = "${custom.opensearch.flinkTaskAnalysisIndex.name}")
    private String flinkTaskAnalysisIndex;


    /**
     * 分页查询作业
     *
     * @param req
     * @return
     */
    @Override
    public CommonStatus<?> pageJobs(DiagnosisAdviceListReq req) throws Exception {
        Map<String, Object> termQuery = req.getTermQuery();
        Map<String, SortOrder> sort = req.getSortOrder();
        Map<String, Object[]> rangeConditions = req.getRangeCondition();

        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, rangeConditions, sort, null);
        Long count = openSearchService.count(builder, flinkTaskAnalysisIndex + "-*");

        builder.from(req.getFrom()).size(req.getSize());

        List<FlinkTaskAnalysis> items = openSearchService.find(FlinkTaskAnalysis.class, builder, flinkTaskAnalysisIndex + "-*");
        List<FlinkTaskAnalysisInfo> infos = items.stream().map(FlinkTaskAnalysisInfo::from).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("list", infos);
        resp.put("total", count);
        resp.put("totalPage", (count + req.getPageSize()) / req.getPageSize());
        return CommonStatus.success(resp);
    }

    public FlinkStatisticsData getStatisticsData(UserInfo userInfo, long startTimestamp, long endTimestamp) throws Exception {
        FlinkStatisticsData statisticsData = new FlinkStatisticsData();

        // 不包含当天0点
        endTimestamp = endTimestamp - 1000; // millis

        Map<String, Object> termQuery = new HashMap<>();
        if (!userInfo.isAdmin()) {
            termQuery.put("users.username", userInfo.getUsername());
        }
        Map<String, Object[]> rangeQuery = new HashMap<>();
        rangeQuery.put("createTime",
                new Object[]{DateUtil.timestampToUTCDate(startTimestamp), DateUtil.timestampToUTCDate(endTimestamp)});

        SearchSourceBuilder searchSourceBuilder =
                openSearchService.genSearchBuilder(termQuery, rangeQuery, null, null);
        // 诊断作业数
        long jobCount = openSearchService.count(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");

        // 异常作业数
        termQuery.put("diagnosisResourceType", new Integer[]{4});
        searchSourceBuilder = openSearchService.genSearchBuilder(termQuery, rangeQuery, null, null);
        long abnormalJobCount = openSearchService.count(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");

        // 异常占比
        double abnormalJobRatio = jobCount == 0 ? 0 : (double) abnormalJobCount / jobCount;

        statisticsData.setJobCount(jobCount);
        statisticsData.setExceptionJobCount(abnormalJobCount);
        statisticsData.setExceptionJobRatio(abnormalJobRatio);

        // 优化资源作业数： 2， 3
        Map<String, Object> resourceTermQuery = new HashMap<>();
        if (!userInfo.isAdmin()) {
            resourceTermQuery.put("users.username", userInfo.getUsername());
        }
        resourceTermQuery.put("diagnosisResourceType", new Integer[]{2, 3});
        searchSourceBuilder = openSearchService.genSearchBuilder(resourceTermQuery, rangeQuery, null, null);
        long resourceJobCount = openSearchService.count(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");

        // 占比
        double resourceJobRatio = jobCount == 0 ? 0 : (double) resourceJobCount / jobCount;

        statisticsData.setResourceJobCount(resourceJobCount);
        statisticsData.setResourceJobRatio(resourceJobRatio);

        // 总的CPU
        Map<String, Object> cpuTermQuery = new HashMap<>();
        if (!userInfo.isAdmin()) {
            cpuTermQuery.put("users.username", userInfo.getUsername());
        }
        searchSourceBuilder = openSearchService.genSearchBuilder(cpuTermQuery, rangeQuery, null, null);
        searchSourceBuilder.aggregation(AggregationBuilders.sum("totalCPU").field("totalCoreNum"));
        Aggregations aggregationTotalCpu = openSearchService.findRawAggregations(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");
        ParsedSum totalCPU = aggregationTotalCpu.get("totalCPU");
        double totalCPUCount = totalCPU.getValue();


        // 可优化CPU数
        cpuTermQuery.put("diagnosisResourceType", new Integer[]{2});
        searchSourceBuilder = openSearchService.genSearchBuilder(cpuTermQuery, rangeQuery, null, null);
        searchSourceBuilder.aggregation(AggregationBuilders.sum("decrCPU").field("cutCoreNum"));
        Aggregations aggregationCpu = openSearchService.findRawAggregations(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");

        ParsedSum decrCPU = aggregationCpu.get("decrCPU");
        double decrCPUCount = decrCPU.getValue();

        // 可优化CPU占比
        double decrCPURatio = totalCPUCount == 0 ? 0 : decrCPUCount / totalCPUCount;

        statisticsData.setTotalCPUCount(totalCPUCount);
        statisticsData.setDecrCPUCount(decrCPUCount);
        statisticsData.setDecrCPURatio(decrCPURatio);

        // 内存总数
        Map<String, Object> memTermQuery = new HashMap<>();
        if (!userInfo.isAdmin()) {
            memTermQuery.put("users.username", userInfo.getUsername());
        }
        searchSourceBuilder = openSearchService.genSearchBuilder(memTermQuery, rangeQuery, null, null);
        searchSourceBuilder.aggregation(AggregationBuilders.sum("totalMemory").field("totalMemNum"));
        Aggregations aggregationTotalMemory = openSearchService.findRawAggregations(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");

        ParsedSum totalMemory = aggregationTotalMemory.get("totalMemory");
        double totalMemoryNum = totalMemory.getValue();

        // 可优化内存数
        memTermQuery.put("diagnosisResourceType", new Integer[]{3});
        searchSourceBuilder = openSearchService.genSearchBuilder(memTermQuery, rangeQuery, null, null);
        searchSourceBuilder.aggregation(AggregationBuilders.sum("dercMemory").field("cutMemNum"));
        Aggregations aggregationMemory = openSearchService.findRawAggregations(searchSourceBuilder, flinkTaskAnalysisIndex + "-*");

        ParsedSum decrMemory = aggregationMemory.get("dercMemory");
        double decrMemoryNum = decrMemory.getValue();

        // 可优化占比
        double decrMemoryRatio = totalMemoryNum == 0 ? 0 : decrMemoryNum / totalMemoryNum;

        statisticsData.setTotalMemory(totalMemoryNum);
        statisticsData.setDecrMemory(decrMemoryNum);
        statisticsData.setDecrMemoryRatio(decrMemoryRatio);
        return statisticsData;
    }

    /**
     * 概览值
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public DiagnosisGeneralViewNumberResp getGeneralViewNumber(DiagnosisGeneralViewReq request) throws Exception {
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();

        // 本周期数据
        PeriodTime periodTime = new PeriodTime(1);
        long thisEndTimestamp = periodTime.getEndTimestamp();
        long thisStartTimestamp = periodTime.getStartTimestamp();
        // debug for test
//        long thisEndTimestamp = System.currentTimeMillis();
//        long thisStartTimestamp = thisEndTimestamp - 24 * 3600 * 1000L;
        CompletableFuture<FlinkStatisticsData> thisPeriodCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getStatisticsData(userInfo, thisStartTimestamp, thisEndTimestamp);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        // 环比数据
        long lastEndTimestamp = thisEndTimestamp - 24 * 3600 * 1000L;
        long lastStartTimestamp = thisStartTimestamp - 24 * 3600 * 1000L;
        CompletableFuture<FlinkStatisticsData> lastPeriodCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getStatisticsData(userInfo, lastStartTimestamp, lastEndTimestamp);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        // 同比数据
        long lastWeekEndTimestamp = thisEndTimestamp - 7 * 24 * 3600 * 1000L;
        long lastWeekStartTimestamp = thisStartTimestamp - 7 * 24 * 3600 * 1000L;
        CompletableFuture<FlinkStatisticsData> lastWeekPeriodCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getStatisticsData(userInfo, lastWeekStartTimestamp, lastWeekEndTimestamp);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        FlinkStatisticsData thisStatisticsData = new FlinkStatisticsData();
        try {
            thisStatisticsData = thisPeriodCompletableFuture.get();
        } catch (Exception e) {
            log.error("get this period failed, msg:", e);
        }

        FlinkStatisticsData lastStatisticsData = new FlinkStatisticsData();
        try {
            lastStatisticsData = lastPeriodCompletableFuture.get();
        } catch (Exception e) {
            log.error("get last day period failed, msg:", e);
        }

        FlinkStatisticsData lastWeekStatisticsData = new FlinkStatisticsData();
        try {
            lastWeekStatisticsData = lastWeekPeriodCompletableFuture.get();
        } catch (Exception e) {
            log.error("get last week period failed, msg:", e);
        }

        // 异常作业 环比
        double exceptionChainRatio = lastStatisticsData.getExceptionJobRatio() == 0 ? 1
                : (thisStatisticsData.getExceptionJobRatio() - lastStatisticsData.getExceptionJobRatio())
                / lastStatisticsData.getExceptionJobRatio();
        if (thisStatisticsData.getExceptionJobRatio() - lastStatisticsData.getExceptionJobRatio() == 0) {
            exceptionChainRatio = 0.0;
        }

        // 异常作业 同比
        double exceptionDayOnDayRatio = lastWeekStatisticsData.getExceptionJobRatio() == 0 ? 1
                : (thisStatisticsData.getExceptionJobRatio() - lastWeekStatisticsData.getExceptionJobRatio())
                / lastWeekStatisticsData.getExceptionJobRatio();
        if (thisStatisticsData.getExceptionJobRatio() - lastWeekStatisticsData.getExceptionJobRatio() == 0) {
            exceptionDayOnDayRatio = 0.0;
        }

        // 可优化资源作业数 环比
        double resourceJobNumChainRatio = lastStatisticsData.getResourceJobRatio() == 0 ? 1
                : (thisStatisticsData.getResourceJobRatio() - lastStatisticsData.getResourceJobRatio())
                / lastStatisticsData.getResourceJobRatio();
        if (thisStatisticsData.getResourceJobRatio() - lastStatisticsData.getResourceJobRatio() == 0) {
            resourceJobNumChainRatio = 0.0;
        }

        // 可优化资源作业数 同比
        double resourceJobNumDayOnDayRatio = lastWeekStatisticsData.getResourceJobRatio() == 0 ? 1
                : (thisStatisticsData.getResourceJobRatio() - lastWeekStatisticsData.getResourceJobRatio())
                / lastWeekStatisticsData.getResourceJobRatio();
        if (thisStatisticsData.getResourceJobRatio() - lastWeekStatisticsData.getResourceJobRatio() == 0) {
            resourceJobNumDayOnDayRatio = 0.0;
        }

        // 可优化CPU数 环比
        double resourceCpuNumChainRatio = lastStatisticsData.getDecrCPURatio() == 0 ? 1
                : (thisStatisticsData.getDecrCPURatio() - lastStatisticsData.getDecrCPURatio())
                / lastStatisticsData.getDecrCPURatio();
        if (thisStatisticsData.getDecrCPURatio() - lastStatisticsData.getDecrCPURatio() == 0) {
            resourceCpuNumChainRatio = 0.0;
        }

        // 可优化CPU数 同比
        double resourceCpuNumDayOnDayRatio = lastWeekStatisticsData.getDecrCPURatio() == 0 ? 1
                : (thisStatisticsData.getDecrCPURatio() - lastWeekStatisticsData.getDecrCPURatio())
                / lastWeekStatisticsData.getDecrCPURatio();
        if (thisStatisticsData.getDecrCPURatio() - lastWeekStatisticsData.getDecrCPURatio() == 0) {
            resourceCpuNumDayOnDayRatio = 0.0;
        }

        // 可优化内存 环比
        double resourceMemoryNumChainRatio = lastStatisticsData.getDecrMemoryRatio() == 0 ? 1
                : (thisStatisticsData.getDecrMemoryRatio() - lastStatisticsData.getDecrMemoryRatio())
                / lastStatisticsData.getDecrMemoryRatio();
        if (thisStatisticsData.getDecrMemoryRatio() - lastStatisticsData.getDecrMemoryRatio() == 0) {
            resourceMemoryNumChainRatio = 0.0;
        }

        // 可优化内存 同比
        double resourceMemoryNumDayOnDayRatio = lastWeekStatisticsData.getDecrMemoryRatio() == 0 ? 1
                : (thisStatisticsData.getDecrMemoryRatio() - lastWeekStatisticsData.getDecrMemoryRatio())
                / lastWeekStatisticsData.getDecrMemoryRatio();
        if (thisStatisticsData.getDecrMemoryRatio() - lastWeekStatisticsData.getDecrMemoryRatio() == 0) {
            resourceMemoryNumDayOnDayRatio = 0.0;
        }

        // 构造数据返回
        GeneralViewNumberDto generalViewNumber = new GeneralViewNumberDto();
        generalViewNumber.setBaseTaskCntSum((int) thisStatisticsData.getJobCount());
        generalViewNumber.setExceptionTaskCntSum((int) thisStatisticsData.getExceptionJobCount());
        generalViewNumber.setResourceTaskCntSum((int) thisStatisticsData.getResourceJobCount());
        generalViewNumber.setCutCoreNumSum((int) thisStatisticsData.getDecrCPUCount());
        generalViewNumber.setTotalCoreNumSum((int) thisStatisticsData.getTotalCPUCount());
        generalViewNumber.setCutMemNumSum((int) thisStatisticsData.getDecrMemory() / 1024); // 单位GB
        generalViewNumber.setTotalMemNumSum((int) thisStatisticsData.getTotalMemory() / 1024);

        GeneralViewNumberDto generalViewNumberDay1Before = new GeneralViewNumberDto();
        generalViewNumberDay1Before.setBaseTaskCntSum((int) lastStatisticsData.getJobCount());
        generalViewNumberDay1Before.setExceptionTaskCntSum((int) lastStatisticsData.getExceptionJobCount());
        generalViewNumberDay1Before.setResourceTaskCntSum((int) lastStatisticsData.getResourceJobCount());
        generalViewNumberDay1Before.setCutCoreNumSum((int) lastStatisticsData.getDecrCPUCount());
        generalViewNumberDay1Before.setTotalCoreNumSum((int) lastStatisticsData.getTotalCPUCount());
        generalViewNumberDay1Before.setCutMemNumSum((int) lastStatisticsData.getDecrMemory() / 1024); // 单位GB
        generalViewNumberDay1Before.setTotalMemNumSum((int) lastStatisticsData.getTotalMemory() / 1024);

        GeneralViewNumberDto generalViewNumberDay7Before = new GeneralViewNumberDto();
        generalViewNumberDay7Before.setBaseTaskCntSum((int) lastWeekStatisticsData.getJobCount());
        generalViewNumberDay7Before.setExceptionTaskCntSum((int) lastWeekStatisticsData.getExceptionJobCount());
        generalViewNumberDay7Before.setResourceTaskCntSum((int) lastWeekStatisticsData.getResourceJobCount());
        generalViewNumberDay7Before.setCutCoreNumSum((int) lastWeekStatisticsData.getDecrCPUCount());
        generalViewNumberDay7Before.setTotalCoreNumSum((int) lastWeekStatisticsData.getTotalCPUCount());
        generalViewNumberDay7Before.setCutMemNumSum((int) lastWeekStatisticsData.getDecrMemory() / 1024); // 单位GB
        generalViewNumberDay7Before.setTotalMemNumSum((int) lastWeekStatisticsData.getTotalMemory() / 1024);

        DiagnosisGeneralViewNumberResp diagnosisGeneralViewNumberResp = new DiagnosisGeneralViewNumberResp();
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDto(generalViewNumber);
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDtoDay1Before(generalViewNumberDay1Before);
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDtoDay7Before(generalViewNumberDay7Before);

        diagnosisGeneralViewNumberResp.setAbnormalJobNumChainRatio((float) exceptionChainRatio);
        diagnosisGeneralViewNumberResp.setAbnormalJobNumDayOnDay((float) exceptionDayOnDayRatio);
        diagnosisGeneralViewNumberResp.setAbnormalJobNumRatio((float) thisStatisticsData.getExceptionJobRatio());

        diagnosisGeneralViewNumberResp.setResourceCpuNumChainRatio((float) resourceCpuNumChainRatio);
        diagnosisGeneralViewNumberResp.setResourceCpuNumDayOnDay((float) resourceCpuNumDayOnDayRatio);
        diagnosisGeneralViewNumberResp.setResourceCpuNumRatio((float) thisStatisticsData.getDecrCPURatio());

        diagnosisGeneralViewNumberResp.setResourceJobNumChainRatio((float) resourceJobNumChainRatio);
        diagnosisGeneralViewNumberResp.setResourceJobNumDayOnDay((float) resourceJobNumDayOnDayRatio);
        diagnosisGeneralViewNumberResp.setResourceJobNumRatio((float) thisStatisticsData.getResourceJobRatio());

        diagnosisGeneralViewNumberResp.setResourceMemoryNumChainRatio((float) resourceMemoryNumChainRatio);
        diagnosisGeneralViewNumberResp.setResourceMemoryNumDayOnDay((float) resourceMemoryNumDayOnDayRatio);
        diagnosisGeneralViewNumberResp.setResourceMemoryNumRatio((float) thisStatisticsData.getDecrMemoryRatio());

        diagnosisGeneralViewNumberResp.setCpuUnit("个");
        diagnosisGeneralViewNumberResp.setMemoryUnit("GB");
        return diagnosisGeneralViewNumberResp;
    }

    /**
     * 获取概览趋势（内存、CPU、数量）
     *
     * @param request
     * @return
     */
    @Override
    public DiagnosisGeneralViewTrendResp getGeneralViewTrend(DiagnosisGeneralViewReq request) throws Exception {
        Map<String, Object[]> rangeConditions = request.getRangeConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(null, rangeConditions, null, null);


        List<IndicatorData> cutCoreData = openSearchService.sumAggregationByDay(builder, request.getStartTs(), request.getEndTs(), flinkTaskAnalysisIndex,
                "createTime", "cutCoreNum");

        List<IndicatorData> totalCoreData = openSearchService.sumAggregationByDay(builder, request.getStartTs(), request.getEndTs(), flinkTaskAnalysisIndex,
                "createTime", "totalCoreNum");

        List<IndicatorData> cutMemData = openSearchService.sumAggregationByDay(builder, request.getStartTs(), request.getEndTs(), flinkTaskAnalysisIndex,
                "createTime", "cutMemNum");

        List<IndicatorData> totalMemData = openSearchService.sumAggregationByDay(builder, request.getStartTs(), request.getEndTs(), flinkTaskAnalysisIndex,
                "createTime", "totalMemNum");

        List<IndicatorData> exceptionData = openSearchService.countDocByDay(builder, request.getStartTs(), request.getEndTs(), flinkTaskAnalysisIndex,
                "createTime");

        List<IndicatorData> totalData = openSearchService.countDocByDay(builder, request.getStartTs(), request.getEndTs(), flinkTaskAnalysisIndex,
                "createTime");

        if (cutMemData != null) {
            cutMemData.forEach(d -> d.setCount(d.getCount() / 1024.0)); // GB
        }
        if (totalMemData != null) {
            totalMemData.forEach(d -> d.setCount(d.getCount() / 1024.0)); // GB
        }
        TrendGraph cpuTrend = new TrendGraph();
        cpuTrend.setName("CPU消耗趋势");
        cpuTrend.setUnit("core");

        LineGraph cpuDecrLine = new LineGraph();
        cpuDecrLine.setName("可优化CPU数");
        cpuDecrLine.setData(cutCoreData);
        cpuTrend.setJobUsage(cpuDecrLine);

        log.info("totalCoreNum=>" + totalCoreData);
        LineGraph cpuTotalLine = new LineGraph();
        cpuTotalLine.setName("总CPU消耗数");
        cpuTotalLine.setData(totalCoreData);
        cpuTrend.setTotalUsage(cpuTotalLine);

        TrendGraph memTrend = new TrendGraph();
        memTrend.setName("内存消耗趋势");
        memTrend.setUnit("GB");

        log.info("cutMemData=>" + cutMemData);
        LineGraph memDecrLine = new LineGraph();
        memDecrLine.setName("可优化内存数");
        memDecrLine.setData(cutMemData);
        memTrend.setJobUsage(memDecrLine);

        log.info("totalMemNum=>" + totalMemData);
        LineGraph memTotalLine = new LineGraph();
        memTotalLine.setName("总内存消耗数");
        memTotalLine.setData(totalMemData);
        memTrend.setTotalUsage(memTotalLine);

        TrendGraph jobNumTrend = new TrendGraph();
        jobNumTrend.setName("任务数趋势");
        jobNumTrend.setUnit("个");

        LineGraph exceptionLine = new LineGraph();
        exceptionLine.setName("异常任务数");
        exceptionLine.setData(exceptionData);
        jobNumTrend.setJobUsage(exceptionLine);

        LineGraph totalNumLine = new LineGraph();
        totalNumLine.setName("总任务数");
        totalNumLine.setData(totalData);
        jobNumTrend.setTotalUsage(totalNumLine);

        DiagnosisGeneralViewTrendResp diagnosisGeneralViewTrendResp = new DiagnosisGeneralViewTrendResp();
        diagnosisGeneralViewTrendResp.setCpuTrend(cpuTrend);
        diagnosisGeneralViewTrendResp.setMemoryTrend(memTrend);
        diagnosisGeneralViewTrendResp.setJobNumberTrend(jobNumTrend);
        return diagnosisGeneralViewTrendResp;
    }

    /**
     * 获取概览分布
     *
     * @param request
     * @return
     */
    public DiagnosisGeneralVIewDistributeResp getGeneralViewDistribute(DiagnosisGeneralViewReq request) throws Exception {
        Map<String, Object[]> rangeConditions = request.getRangeConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(null, rangeConditions, null, null);
        TermsAggregationBuilder termsAggregationBuilder =
                AggregationBuilders.terms("distribution").field("diagnosisTypes.keyword").size(100);

        SumAggregationBuilder cpuBuilder = AggregationBuilders.sum("cpu").field("totalCoreNum");
        SumAggregationBuilder memBuilder = AggregationBuilders.sum("mem").field("totalMemNum");

        termsAggregationBuilder.subAggregation(cpuBuilder);
        termsAggregationBuilder.subAggregation(memBuilder);

        builder.aggregation(termsAggregationBuilder);

        Aggregations aggregations = openSearchService.findRawAggregations(builder, flinkTaskAnalysisIndex + "-*");
        Terms terms = aggregations.get("distribution");
        DistributionGraph cpuGraph = new DistributionGraph();
        cpuGraph.setName("CPU资源消耗分布");
        DistributionGraph memGraph = new DistributionGraph();
        memGraph.setName("内存资源消耗分布");
        DistributionGraph numGraph = new DistributionGraph();
        numGraph.setName("任务数量分布图");

        List<DistributionData> cpuList = new ArrayList<>();
        List<DistributionData> memList = new ArrayList<>();
        List<DistributionData> numList = new ArrayList<>();

        for (Terms.Bucket bucket : terms.getBuckets()) {
            DistributionData cpuData = new DistributionData();
            DistributionData memData = new DistributionData();
            DistributionData numData = new DistributionData();
            String name = bucket.getKeyAsString();
            cpuData.setName(name);
            memData.setName(name);
            numData.setName(name);

            ParsedSum cpu = bucket.getAggregations().get("cpu");
            ParsedSum mem = bucket.getAggregations().get("mem");

            cpuData.setValue(cpu.getValue());
            memData.setValue(mem.getValue());
            numData.setValue((double) bucket.getDocCount());

            cpuList.add(cpuData);
            memList.add(memData);
            numList.add(numData);
        }

        cpuGraph.setData(cpuList);
        memGraph.setData(memList);
        numGraph.setData(numList);

        DiagnosisGeneralVIewDistributeResp graph = new DiagnosisGeneralVIewDistributeResp();

        graph.setCpu(cpuGraph);
        graph.setMem(memGraph);
        graph.setNum(numGraph);
        return graph;
    }

    /**
     * 获取诊断报告
     *
     * @param request
     * @return
     */

    public DiagnosisReportResp getReport(ReportDetailReq request) throws Exception {
        DiagnosisReportResp diagnosisReportResp = new DiagnosisReportResp();

        String id = request.getId(); // FlinkTaskAnalysisId

        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("_id", id);
        List<FlinkTaskAnalysis> flinkTaskAnalyses = openSearchService.find(FlinkTaskAnalysis.class, termQuery, flinkTaskAnalysisIndex + "-*");

        if (flinkTaskAnalyses.size() == 0) {
            return diagnosisReportResp;
        }

        diagnosisReportResp.setFlinkTaskDiagnosis(FlinkTaskAnalysisInfo.from(flinkTaskAnalyses.get(0)));

        termQuery = new HashMap<>();
        termQuery.put("flinkTaskAnalysisId.keyword", id);
        List<FlinkTaskReport> flinkTaskReports = openSearchService.find(FlinkTaskReport.class, termQuery, flinkReportIndex + "-*");

        if (flinkTaskReports.size() == 0) {
            return diagnosisReportResp;
        }

        List<String> reports = new ArrayList<>();
        for (FlinkTaskReport report : flinkTaskReports) {
            reports.add(report.getReportJson());
        }

        diagnosisReportResp.setReports(reports);
        return diagnosisReportResp;
    }

    /**
     * 批量上报数据
     *
     * @param apps
     * @return
     */
    public CommonStatus<?> batchMetadata(List<FlinkTaskApp> apps) {
        return null;
    }
}
