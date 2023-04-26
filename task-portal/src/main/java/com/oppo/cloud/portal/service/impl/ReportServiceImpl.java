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

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.mapper.ProjectMapper;
import com.oppo.cloud.model.Project;
import com.oppo.cloud.model.ProjectExample;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.dao.TaskInstanceExtendMapper;
import com.oppo.cloud.portal.domain.report.*;
import com.oppo.cloud.portal.domain.statistics.PeriodTime;
import com.oppo.cloud.portal.domain.statistics.StatisticsData;
import com.oppo.cloud.portal.domain.task.IndicatorData;
import com.oppo.cloud.portal.domain.task.JobsRequest;
import com.oppo.cloud.portal.domain.task.UserInfo;
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.service.ReportService;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Comparator.comparing;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Value(value = "${custom.elasticsearch.jobIndex.name}")
    private String jobIndex;

    @Value(value = "${custom.elasticsearch.jobInstanceIndex.name}")
    private String jobInstanceIndex;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private TaskInstanceExtendMapper taskInstanceExtendMapper;


    @Override
    public StatisticsData getStatisticsData(String projectName) throws Exception {
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();
        PeriodTime periodTime = new PeriodTime(1);

        // 本周期
        long endTimestamp = periodTime.getEndTimestamp();
        long startTimestamp = periodTime.getStartTimestamp();
        CompletableFuture<StatisticsData> thisPeriodCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getStaticsDataByReportData(userInfo, projectName, startTimestamp, endTimestamp);
            } catch (Exception e) {
                throw new CompletionException(e);
            }

        });
        // 环比
        long lastStartTimestamp = periodTime.getLastDayPeriod().getStartTimestamp();
        long lastEndTimestamp = periodTime.getLastDayPeriod().getEndTimestamp();
        CompletableFuture<StatisticsData> lastPeriodCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getStaticsDataByReportData(userInfo, projectName, lastStartTimestamp, lastEndTimestamp);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        // 同比
        long lastWeekStartTimestamp = periodTime.getLastWeekPeriod().getStartTimestamp();
        long lastWeekEndTimestamp = periodTime.getLastWeekPeriod().getEndTimestamp();
        CompletableFuture<StatisticsData> lastWeekPeriodCompletableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getStaticsDataByReportData(userInfo, projectName, lastWeekStartTimestamp, lastWeekEndTimestamp);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        StatisticsData result = new StatisticsData();
        try {
            result = thisPeriodCompletableFuture.get();
        } catch (Exception e) {
            log.error("get this period failed, msg:", e);
        }
        StatisticsData lastResult = new StatisticsData();
        try {
            lastResult = lastPeriodCompletableFuture.get();
        } catch (Exception e) {
            log.error("get last day period failed,msg:", e);
        }
        StatisticsData lastWeekResult = new StatisticsData();
        try {
            lastWeekResult = lastWeekPeriodCompletableFuture.get();
        } catch (Exception e) {
            log.error("get last week period failed,msg:", e);
        }
        if (lastResult == null || lastWeekResult == null) {
            log.error("search statistics data is null");
            return result;
        }

        // 异常任务数占比的环比
        double exceptionChainRatio = lastResult.getAbnormalJobNumRatio() == 0 ? 1
                : (result.getAbnormalJobNumRatio() - lastResult.getAbnormalJobNumRatio())
                / lastResult.getAbnormalJobNumRatio();
        if (result.getAbnormalJobNumRatio() - lastResult.getAbnormalJobNumRatio() == 0) {
            exceptionChainRatio = 0.0;
        }
        result.setAbnormalJobNumChainRatio(exceptionChainRatio);
        // 异常任务数占比的同比
        double exceptionDayOnDayRatio = lastWeekResult.getAbnormalJobNumRatio() == 0 ? 1
                : (result.getAbnormalJobNumRatio() - lastWeekResult.getAbnormalJobNumRatio())
                / lastWeekResult.getAbnormalJobNumRatio();
        if (result.getAbnormalJobNumRatio() - lastWeekResult.getAbnormalJobNumRatio() == 0) {
            exceptionDayOnDayRatio = 0.0;
        }
        result.setAbnormalJobNumDayOnDay(exceptionDayOnDayRatio);

        // 异常实例数占比的环比
        double exceptionJobInstanceChainRatio = lastResult.getAbnormalJobInstanceNumRatio() == 0 ? 1
                : (result.getAbnormalJobInstanceNumRatio() - lastResult.getAbnormalJobInstanceNumRatio())
                / lastResult.getAbnormalJobInstanceNumRatio();
        if (result.getAbnormalJobInstanceNumRatio() - lastResult.getAbnormalJobInstanceNumRatio() == 0) {
            exceptionJobInstanceChainRatio = 0.0;
        }
        result.setAbnormalJobInstanceNumChainRatio(exceptionJobInstanceChainRatio);

        // 异常实例数占比的同比
        double exceptionExecutionDateDayOnDayCostRatio = lastWeekResult.getAbnormalJobInstanceNumRatio() == 0 ? 1
                : (result.getAbnormalJobInstanceNumRatio() - lastWeekResult.getAbnormalJobInstanceNumRatio())
                / lastWeekResult.getAbnormalJobInstanceNumRatio();
        if (result.getAbnormalJobInstanceNumRatio() - lastWeekResult.getAbnormalJobInstanceNumRatio() == 0) {
            exceptionExecutionDateDayOnDayCostRatio = 0.0;
        }
        result.setAbnormalJobInstanceNumDayOnDay(exceptionExecutionDateDayOnDayCostRatio);

        // 异常任务cpu占比的环比
        double exceptionCpuChainRatio = lastResult.getAbnormalJobCpuNumRatio() == 0 ? 1
                : (result.getAbnormalJobCpuNumRatio() - lastResult.getAbnormalJobCpuNumRatio())
                / lastResult.getAbnormalJobCpuNumRatio();
        if (result.getAbnormalJobCpuNumRatio() - lastResult.getAbnormalJobCpuNumRatio() == 0) {
            exceptionCpuChainRatio = 0.0;
        }
        result.setAbnormalJobCpuNumChainRatio(exceptionCpuChainRatio);

        // 异常任务cpu占比的同比
        double exceptionDayOnDayCpuRatio = lastWeekResult.getAbnormalJobCpuNumRatio() == 0 ? 1
                : (result.getAbnormalJobCpuNumRatio() - lastWeekResult.getAbnormalJobCpuNumRatio())
                / lastWeekResult.getAbnormalJobCpuNumRatio();
        if (result.getAbnormalJobCpuNumRatio() - lastWeekResult.getAbnormalJobCpuNumRatio() == 0) {
            exceptionDayOnDayCpuRatio = 0.0;
        }
        result.setAbnormalJobCpuNumDayOnDay(exceptionDayOnDayCpuRatio);

        // 异常任务内存占比的环比
        double exceptionMemoryChainRatio = lastResult.getAbnormalJobMemoryNumRatio() == 0 ? 1
                : (result.getAbnormalJobMemoryNumRatio() - lastResult.getAbnormalJobMemoryNumRatio())
                / lastResult.getAbnormalJobMemoryNumRatio();
        if (result.getAbnormalJobMemoryNumRatio() - lastResult.getAbnormalJobMemoryNumRatio() == 0) {
            exceptionMemoryChainRatio = 0.0;
        }
        result.setAbnormalJobMemoryNumChainRatio(exceptionMemoryChainRatio);

        // 异常任务内存占比的同比
        double exceptionDayOnDayMemoryRatio = lastWeekResult.getAbnormalJobMemoryNumRatio() == 0 ? 1
                : (result.getAbnormalJobMemoryNumRatio() - lastWeekResult.getAbnormalJobMemoryNumRatio())
                / lastWeekResult.getAbnormalJobMemoryNumRatio();
        if (result.getAbnormalJobMemoryNumRatio() - lastWeekResult.getAbnormalJobMemoryNumRatio() == 0) {
            exceptionDayOnDayMemoryRatio = 0.0;
        }
        result.setAbnormalJobMemoryNumDayOnDay(exceptionDayOnDayMemoryRatio);

        convertStatisticsData(result);
        return result;
    }

    private void convertStatisticsData(StatisticsData result) {
        // vcoreSeconds
        String cpuUnit = UnitUtil.getTimeUnit(result.getJobCpuNum());
        result.setCpuUnit(String.format("vcore·%s", cpuUnit));
        result.setAbnormalJobCpuNum(UnitUtil.convertCpuUnit(cpuUnit, result.getAbnormalJobCpuNum()));
        result.setJobCpuNum(UnitUtil.convertCpuUnit(cpuUnit, result.getJobCpuNum()));

        // memorySeconds
        String memUnit = UnitUtil.getTimeUnit(result.getJobMemoryNum());
        result.setMemoryUnit(String.format("GB·%s", memUnit));
        result.setAbnormalJobMemoryNum(UnitUtil.convertMemoryUnit(memUnit, result.getAbnormalJobMemoryNum()));
        result.setJobMemoryNum(UnitUtil.convertMemoryUnit(memUnit, result.getJobMemoryNum()));
    }

    public StatisticsData getStaticsDataByReportData(UserInfo userInfo, String projectName, long startTimestamp, long endTimestamp)
            throws Exception {
        StatisticsData statisticsData = new StatisticsData();

        // 不包含当天0点
        endTimestamp = endTimestamp - 1000;
        Map<String, Object> termQuery = new HashMap<>();
        if (StringUtils.isNotBlank(projectName)) {
            termQuery.put("projectName.keyword", projectName);
        }
        if (!userInfo.isAdmin()) {
            termQuery.put("users.username", userInfo.getUsername());
        }
        Map<String, Object[]> rangeQuery = new HashMap<>();
        rangeQuery.put("executionDate",
                new Object[]{DateUtil.timestampToUTCDate(startTimestamp), DateUtil.timestampToUTCDate(endTimestamp)});

        SearchSourceBuilder searchSourceBuilder =
                elasticSearchService.genSearchBuilder(termQuery, rangeQuery, null, null);

        // 诊断任务数
        AggregationBuilder aggregationBuilderAbnormalJobCount =
                AggregationBuilders.terms("groupByCount")
                        .script(new Script("doc['projectName.keyword'].value+'@@'+doc['flowName.keyword'].value+'@@'+doc['taskName.keyword'].value")
                        ).size(10000);

        searchSourceBuilder.aggregation(aggregationBuilderAbnormalJobCount);

        Aggregations aggregationsGroupByCount =
                elasticSearchService.findRawAggregations(searchSourceBuilder, jobIndex + "-*");
        if (aggregationsGroupByCount == null) {
            return null;
        }
        Terms terms = aggregationsGroupByCount.get("groupByCount");
        int abnormalJobCount = terms.getBuckets().size();
        statisticsData.setAbnormalJobNum(abnormalJobCount);

        // 活跃任务数
        int jobCount = taskInstanceExtendMapper.searchJobCount(new Date(startTimestamp), new Date(endTimestamp));
        statisticsData.setJobNum(jobCount);

        // 诊断实例数
        long abnormalJobInstanceCount = elasticSearchService.count(searchSourceBuilder, jobIndex + "-*");
        statisticsData.setAbnormalJobInstanceNum((int) abnormalJobInstanceCount);

        // 运行实例数
        int jobInstanceCount =
                taskInstanceExtendMapper.searchJobInstanceCount(new Date(startTimestamp), new Date(endTimestamp));
        statisticsData.setJobInstanceNum(jobInstanceCount);

        double abnormalJobRatio = jobCount == 0 ? 0 : (double) abnormalJobCount / jobCount;
        statisticsData.setAbnormalJobNumRatio(abnormalJobRatio);
        double abnormalJobInstanceRatio =
                jobInstanceCount == 0 ? 0 : (double) abnormalJobInstanceCount / jobInstanceCount;
        statisticsData.setAbnormalJobInstanceNumRatio(abnormalJobInstanceRatio);

        // 异常任务CPU、内存统计
        searchSourceBuilder = elasticSearchService.genSearchBuilder(termQuery, rangeQuery, null, null);
        searchSourceBuilder.aggregation(AggregationBuilders.sum("cpu").field("vcoreSeconds"));
        searchSourceBuilder.aggregation(AggregationBuilders.sum("memory").field("memorySeconds"));
        Aggregations aggregationsAbnormalCpuAndMemory =
                elasticSearchService.findRawAggregations(searchSourceBuilder, jobIndex + "-*");
        ParsedSum cpu = aggregationsAbnormalCpuAndMemory.get("cpu");
        ParsedSum memory = aggregationsAbnormalCpuAndMemory.get("memory");
        double abnormalJobInstanceCpu = cpu.getValue();
        statisticsData.setAbnormalJobCpuNum(abnormalJobInstanceCpu);
        double abnormalJobInstanceMemory = memory.getValue();
        statisticsData.setAbnormalJobMemoryNum(abnormalJobInstanceMemory);

        // 全量任务CPU、内存统计
        searchSourceBuilder = elasticSearchService.genSearchBuilder(termQuery, rangeQuery, null, null);
        searchSourceBuilder.aggregation(AggregationBuilders.sum("cpu").field("vcoreSeconds"));
        searchSourceBuilder.aggregation(AggregationBuilders.sum("memory").field("memorySeconds"));
        Aggregations aggregationsJobCpuAndMemory =
                elasticSearchService.findRawAggregations(searchSourceBuilder, jobInstanceIndex + "-*");
        ParsedSum jobCpu = aggregationsJobCpuAndMemory.get("cpu");
        ParsedSum jobMemory = aggregationsJobCpuAndMemory.get("memory");
        double jobInstanceCpu = jobCpu.getValue();
        statisticsData.setJobCpuNum(jobInstanceCpu);
        double jobInstanceMemory = jobMemory.getValue();
        statisticsData.setJobMemoryNum(jobInstanceMemory);

        double abnormalJobCpuNumRatio = jobInstanceCpu == 0 ? 0 : abnormalJobInstanceCpu / jobInstanceCpu;
        double abnormalJobMemoryNumRatio = jobInstanceMemory == 0 ? 0 : abnormalJobInstanceMemory / jobInstanceMemory;

        statisticsData.setAbnormalJobCpuNumRatio(abnormalJobCpuNumRatio);
        statisticsData.setAbnormalJobMemoryNumRatio(abnormalJobMemoryNumRatio);

        return statisticsData;
    }

    /**
     * 获取报告总览图表
     */
    @Override
    public ReportGraph getGraph(ReportRequest reportRequest) throws Exception {

        if ("distribution".equals(reportRequest.getGraphType())) {
            return getDistributionGraph(reportRequest);
        }

        JobsRequest request = new JobsRequest();
        request.setProjectName(reportRequest.getProjectName());
        request.setStart(reportRequest.getStart() * 1000);
        // 不包含结束时间当天
        request.setEnd((reportRequest.getEnd() - 1) * 1000);

        ReportGraph graph = new ReportGraph();

        switch (reportRequest.getGraphType()) {
            case "cpuTrend":
                graph.setTrendGraph(getResourceTrendData(request, "vcoreSeconds"));
                break;
            case "memoryTrend":
                graph.setTrendGraph(getResourceTrendData(request, "memorySeconds"));
                break;
            case "numTrend":
                graph.setTrendGraph(getNumTrendData(request));
                break;
            default:
                return null;
        }
        return graph;
    }

    /**
     * 资源消耗趋势
     */
    public TrendGraph getResourceTrendData(JobsRequest request, String filed) throws Exception {

        TrendGraph trendGraph = new TrendGraph();
        List<IndicatorData> jobUsageTrend = elasticSearchService.sumAggregationByDay(getBuilder(request),
                request.getStart(), request.getEnd(), jobIndex, filed);

        List<IndicatorData> totalUsageTrend = elasticSearchService.sumAggregationByDay(getBuilder(request),
                request.getStart(), request.getEnd(), jobInstanceIndex, filed);
        if (totalUsageTrend == null) {
            log.error("search totalUsageTrend is null");
            return trendGraph;
        }
        LineGraph jobGraph = new LineGraph();
        LineGraph totalGraph = new LineGraph();

        double maxValue = 0.0;

        IndicatorData indicatorData = totalUsageTrend.stream().max(comparing(IndicatorData::getCount)).orElse(null);
        if (indicatorData != null) {
            maxValue = indicatorData.getCount();
        }

        String timeUnit;
        switch (filed) {
            case "vcoreSeconds":
                trendGraph.setName("CPU消耗趋势");
                jobGraph.setName("诊断任务CPU消耗数");
                totalGraph.setName("总CPU消耗数");
                timeUnit = UnitUtil.getTimeUnit(maxValue);
                jobUsageTrend.forEach(data -> data.setCount(UnitUtil.convertCpuUnit(timeUnit, data.getCount())));
                totalUsageTrend.forEach(data -> data.setCount(UnitUtil.convertCpuUnit(timeUnit, data.getCount())));
                trendGraph.setUnit(String.format("vcore·%s", timeUnit));
                break;
            case "memorySeconds":
                trendGraph.setName("内存消耗趋势");
                jobGraph.setName("诊断任务内存消耗数");
                totalGraph.setName("总内存消耗数");
                timeUnit = UnitUtil.getTimeUnit(maxValue / 1024);
                jobUsageTrend.forEach(data -> data.setCount(UnitUtil.convertMemoryUnit(timeUnit, data.getCount())));
                totalUsageTrend.forEach(data -> data.setCount(UnitUtil.convertMemoryUnit(timeUnit, data.getCount())));
                trendGraph.setUnit(String.format("GB·%s", timeUnit));
                break;
            default:
                break;
        }

        jobGraph.setData(jobUsageTrend);
        totalGraph.setData(totalUsageTrend);
        trendGraph.setJobUsage(jobGraph);
        trendGraph.setTotalUsage(totalGraph);
        return trendGraph;
    }

    /**
     * 数量趋势
     */
    public TrendGraph getNumTrendData(JobsRequest request) throws Exception {
        TrendGraph trendGraph = new TrendGraph();
        List<IndicatorData> jobNumTrend = elasticSearchService.countDocByDay(getBuilder(request), request.getStart(),
                request.getEnd(), jobIndex);
        List<IndicatorData> totalNumTrend = elasticSearchService.countDocByDay(getBuilder(request), request.getStart(),
                request.getEnd(), jobInstanceIndex);
        trendGraph.setName("数量趋势");
        LineGraph jobGraph = new LineGraph();
        jobGraph.setName("诊断任务数");
        jobGraph.setData(jobNumTrend);
        LineGraph totalGraph = new LineGraph();
        totalGraph.setName("总任务数");
        totalGraph.setData(totalNumTrend);
        trendGraph.setJobUsage(jobGraph);
        trendGraph.setTotalUsage(totalGraph);
        return trendGraph;
    }

    /**
     * 获取资源/数量分布图
     */
    public ReportGraph getDistributionGraph(ReportRequest reportRequest) throws Exception {
        ReportGraph graph = new ReportGraph();
        Map<String, Object> termQuery = reportRequest.getTermQuery();
        Map<String, Object[]> rangeConditions = reportRequest.getRangeConditions();
        SearchSourceBuilder builder = elasticSearchService.genSearchBuilder(termQuery, rangeConditions, null, null);
        TermsAggregationBuilder termsAggregationBuilder =
                AggregationBuilders.terms("distribution").field("categories.keyword").size(100);

        SumAggregationBuilder vcoreSecondsBuilder = AggregationBuilders.sum("cpu").field("vcoreSeconds");
        SumAggregationBuilder memorySecondsBuilder = AggregationBuilders.sum("mem").field("memorySeconds");
        termsAggregationBuilder.subAggregation(vcoreSecondsBuilder);
        termsAggregationBuilder.subAggregation(memorySecondsBuilder);

        builder.aggregation(termsAggregationBuilder);

        Aggregations aggregations = elasticSearchService.findRawAggregations(builder, jobIndex + "-*");
        if (aggregations == null) {
            log.error("search {} aggregations is null", jobIndex);
            return graph;
        }
        Terms terms = aggregations.get("distribution");
        DistributionGraph cpuGraph = new DistributionGraph();
        cpuGraph.setName("CPU资源消耗分布");
        DistributionGraph memGraph = new DistributionGraph();
        memGraph.setName("内存资源消耗分布");
        DistributionGraph numGraph = new DistributionGraph();
        numGraph.setName("任务数量分布图");

        List<DistributionData> cpuList = new ArrayList<>();
        List<DistributionData> menList = new ArrayList<>();
        List<DistributionData> numList = new ArrayList<>();

        for (Terms.Bucket bucket : terms.getBuckets()) {
            DistributionData cpuData = new DistributionData();
            DistributionData memData = new DistributionData();
            DistributionData numData = new DistributionData();
            String name;
            name = AppCategoryEnum.getAppCategoryOfDesc(bucket.getKeyAsString());
            if (name == null) {
                name = JobCategoryEnum.getJobNameMsg(bucket.getKeyAsString());
            }
            cpuData.setName(name);
            memData.setName(name);
            numData.setName(name);

            ParsedSum cpu = bucket.getAggregations().get("cpu");
            ParsedSum mem = bucket.getAggregations().get("mem");

            cpuData.setValue(cpu.getValue());
            memData.setValue(mem.getValue());
            numData.setValue((double) bucket.getDocCount());

            cpuList.add(cpuData);
            menList.add(memData);
            numList.add(numData);
        }

        cpuGraph.setData(cpuList);
        memGraph.setData(menList);
        numGraph.setData(numList);

        graph.setCpu(cpuGraph);
        graph.setMem(memGraph);
        graph.setNum(numGraph);
        return graph;
    }

    private SearchSourceBuilder getBuilder(JobsRequest request) {
        Map<String, Object> termQuery = request.getTermQuery();
        Map<String, Object[]> rangeConditions = request.getRangeConditions();
        return elasticSearchService.genSearchBuilder(termQuery, rangeConditions, null, null);
    }

    /**
     * 获取项目列表
     */
    @Override
    public Set<String> getProjects() throws Exception {
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();
        Set<String> projectList = new HashSet<>();
        ProjectExample projectExample = new ProjectExample();
        if (!userInfo.isAdmin()) {
            projectExample.createCriteria().andUserIdEqualTo(userInfo.getUserId());
        }
        List<Project> projects = projectMapper.selectByExample(projectExample);
        for (Project project : projects) {
            if (StringUtils.isNotBlank(project.getProjectName())) {
                projectList.add(project.getProjectName());
            }
        }
        return projectList;
    }

}
