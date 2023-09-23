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
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.JobInstance;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.domain.job.Datum;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.TryNumberUtil;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.domain.base.Conclusion;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.domain.diagnose.Table;
import com.oppo.cloud.portal.domain.diagnose.runtime.ChartData;
import com.oppo.cloud.portal.domain.diagnose.runtime.TableData;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.log.LogInfo;
import com.oppo.cloud.portal.domain.task.*;
import com.oppo.cloud.portal.service.*;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.script.Script;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Slf4j
@Service
public class JobServiceImpl implements JobService {

    @Value(value = "${custom.opensearch.jobIndex.name}")
    private String jobsIndex;

    @Value(value = "${custom.opensearch.jobInstanceIndex.name}")
    private String jobIndexIndex;

    @Value(value = "${custom.opensearch.appIndex.name}")
    private String taskAppIndex;

    @Autowired
    private OpenSearchService openSearchService;

    @Autowired
    private LogService logService;

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    private TaskAppService taskAppService;

    @Autowired
    private RedisService redisService;

    /**
     * 搜索作业层列表
     */
    @Override
    public JobsResponse searchJobs(JobsRequest request) throws Exception {
        Map<String, Object> termQuery = request.getTermQuery();
        Map<String, SortOrder> sort = request.getSortOrder();
        Map<String, Object[]> rangeConditions = request.getRangeConditions();

        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, rangeConditions, sort, null);
        Long count = openSearchService.count(builder, jobsIndex + "-*");
        builder.from(request.getFrom()).size(request.getSize());

        List<JobAnalysis> items = openSearchService.find(JobAnalysis.class, builder, jobsIndex + "-*");
        List<JobInfo> jobInfos = items.stream().map(data -> JobInfo.from(data, redisService.get(String.format("%s:%s:%s", data.getProjectName(), data.getFlowName(), data.getTaskName())))).collect(Collectors.toList());

        JobsResponse response = new JobsResponse();
        response.setJobInfos(jobInfos);
        response.setCount(count);
        return response;
    }

    @Override
    public JobAppsRespone searchJobApps(JobDetailRequest jobDetailRequest) throws Exception {
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();
        JobAppsRespone jobAppsRespone = new JobAppsRespone();
        Map<String, Object> termQuery = jobDetailRequest.getTermQuery();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, null, null, null);
        builder.size(1000);
        List<JobAnalysis> items = openSearchService.find(JobAnalysis.class, builder, jobsIndex + "-*");
        if (items.size() == 0) {
            throw new Exception("Not Found this Job");
        }
        JobInfo jobInfo = JobInfo.from(items.get(0), null);
        jobInfo.setTryNumber(TryNumberUtil.updateTryNumber(jobInfo.getTryNumber(), userInfo.getSchedulerType()));
        List<TaskApp> taskApps = openSearchService.find(TaskApp.class, builder, taskAppIndex + "-*");
        Map<String, List<TaskAppInfo>> taskAppMap = this.formatJobApps(taskApps, userInfo.getSchedulerType());
        // 构造完整的taskApp
        for (int i = 0; i <= jobInfo.getTryNumber(); i++) {
            String key = String.format("第%d次执行", i + 1);
            if (!taskAppMap.containsKey(key)) {
                TaskAppInfo taskAppInfo = new TaskAppInfo();
                taskAppInfo.setCategories(null);
                taskAppInfo.setTaskName(jobDetailRequest.getTaskName());
                taskAppInfo.setFlowName(jobDetailRequest.getFlowName());
                taskAppInfo.setProjectName(jobDetailRequest.getProjectName());
                taskAppInfo.setExecutionDate(DateUtil.format(jobDetailRequest.getExecutionDate()));
                taskAppInfo.setDuration("0.00s");
                taskAppInfo.setResource("0 vcore·s 0 GB·s");
                taskAppInfo.setApplicationId("applicationId不存在");
                taskAppMap.put(key, Collections.singletonList(taskAppInfo));
            }
        }
        jobAppsRespone.setJobInfo(jobInfo);
        jobAppsRespone.setTaskApps(taskAppMap);
        return jobAppsRespone;
    }


    @Override
    public List<String> searchJobDiagnose(JobDetailRequest jobDetailRequest) throws Exception {
        List<String> res = new ArrayList<>();
        Map<String, Object> termQuery = jobDetailRequest.getTermQuery();
        Map<String, SortOrder> sortQuery = jobDetailRequest.getSortConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, null, sortQuery, null);
        builder.size(1000);
        List<JobAnalysis> items = openSearchService.find(JobAnalysis.class, builder, jobsIndex + "-*");
        if (items.size() == 0) {
            return res;
        }
        List<TaskApp> taskApps = openSearchService.find(TaskApp.class, builder, taskAppIndex + "-*");
        // 获取Yarn异常日志
        CompletableFuture<String> completableFutureYarn = CompletableFuture.supplyAsync(() -> this.getDiagnosticDetectSum(taskApps));
        // 调度器异常汇总
        CompletableFuture<String> completableFutureOflow = CompletableFuture.supplyAsync(() -> this.getSchedulerDetectSum(jobDetailRequest));
        // app级别的诊断汇总
        CompletableFuture<List<String>> completableFutureApps = CompletableFuture.supplyAsync(() -> this.getJobAppDetectSum(taskApps));
        // 任务级别的任务异常汇总
        List<String> taskCategory = new ArrayList<>();
        JobAnalysis jobAnalysis = items.get(0);
        List<String> categoryCh = JobCategoryEnum.getJobCategoryCh(jobAnalysis.getCategories());
        categoryCh.addAll(AppCategoryEnum.getAppCategoryCh(jobAnalysis.getCategories()));
        for (String category : categoryCh) {
            taskCategory.add(UIUtil.transferRed(category));
        }
        res.add(String.format("该任务发生%s", String.join(",", taskCategory)));
        if (completableFutureYarn.get() != null) {
            res.add(completableFutureYarn.get());
        }
        if (completableFutureOflow.get() != null) {
            res.add(completableFutureOflow.get());
        }
        if (completableFutureApps.get() != null) {
            List<String> appSummary = completableFutureApps.get();
            for (String appException : appSummary) {
                res.add(String.format("检测到%s, 具体情况请点击查看其诊断报告", appException));
            }
        }
        return res;
    }

    @Override
    public Item<TableData<LogInfo>> searchLogInfo(JobDetailRequest jobDetailRequest) throws Exception {
        Item<TableData<LogInfo>> res = new Item<>();
        TableData<LogInfo> tableData = new TableData<>();
        res.setName("异常日志分析");
        res.setConclusion(new Conclusion("运行过程发生错误异常,请根据关键日志和相应的诊断建议进行问题修改", "抓取scheduler中的错误日志"));
        int totalTryNum = jobDetailRequest.getTryNumber();
        for (int i = 0; i <= totalTryNum; i++) {
            jobDetailRequest.setTryNumber(i);
            List<LogInfo> logInfoList = logService.getLogDetect(jobDetailRequest, "scheduler");
            if (logInfoList != null && logInfoList.size() != 0) {
                Table<LogInfo> table = new Table<>();
                table.setDes(String.format("第%d次执行错误日志汇总", i + 1));
                table.setData(logInfoList);
                table.setTitles(LogInfo.getTitles());
                tableData.getTableList().add(table);
            }
        }
        if (tableData.getTableList().size() != 0) {
            res.setItem(tableData);
        }
        res.setType("table");
        return res;
    }

    @Override
    public Item<ChartData> searchDurationTrend(JobDetailRequest jobDetailRequest) throws Exception {
        Item<ChartData> res = new Item<>();
        ChartData chartData = null;
        Map<String, Object> termQuery = jobDetailRequest.getTermQuery();
        Map<String, SortOrder> sortQuery = jobDetailRequest.getSortConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, null, sortQuery, null);
        builder.size(1000);
        List<JobAnalysis> items = openSearchService.find(JobAnalysis.class, builder, jobsIndex + "-*");
        if (items.size() == 0) {
            return res;
        }
        res.setName("运行耗时异常分析");
        JobAnalysis jobAnalysis = items.get(0);
        res.setConclusion(new Conclusion(getDurationConclusion(jobAnalysis), ""));
        List<MetricInfo> metricInfoList = taskInstanceService.getJobDurationTrend(jobDetailRequest);
        if (metricInfoList.size() != 0) {
            chartData = new ChartData();
            Chart<MetricInfo> chart = new Chart<>();
            chart.setDataList(metricInfoList);
            chart.setDes("运行耗时趋势图");
            chart.setUnit("s");
            chart.setX("执行周期");
            chart.setY("运行耗时");
            Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(1);
            dataCategory.put("duration", new Chart.ChartInfo("最大值", UIUtil.ABNORMAL_COLOR));
            chart.setDataCategory(dataCategory);
            chartData.getChartList().add(chart);
            chartData.setThreshold(StringUtils.isNotEmpty(jobAnalysis.getDurationBaseline()) ? UnitUtil.transferSecond(jobAnalysis.getDurationBaseline()) : jobAnalysis.getDuration() * 1.6);
        }
        res.setItem(chartData);
        res.setType("chart");
        return res;
    }

    @Override
    public Item<Datum> searchJobDatum(JobDetailRequest jobDetailRequest) throws Exception {
        Item<Datum> res = new Item<>();
        res.setName("基线时间异常分析");
        res.setType("chart");
        Map<String, Object> termQuery = jobDetailRequest.getTermQuery();
        Map<String, SortOrder> sortQuery = jobDetailRequest.getSortConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, null, sortQuery, null);
        builder.size(1000);
        List<JobAnalysis> items = openSearchService.find(JobAnalysis.class, builder, jobsIndex + "-*");
        if (items.size() == 0) {
            return res;
        }
        JobAnalysis jobAnalysis = items.get(0);
        if (jobAnalysis.getCategories().contains(JobCategoryEnum.endTimeAbnormal.name())) {
            res.setConclusion(new Conclusion(String.format("本任务结束时间为%s, 基线时间为%s, 运行发生晚点", DateUtil.format(jobAnalysis.getEndTime()), jobAnalysis.getEndTimeBaseline()), "根据该任务的上下由关系，分析本次任务晚点的原因"));
            Datum datum = taskInstanceService.getJobDatum(jobDetailRequest);
            res.setItem(datum);
        }

        return res;
    }

    /**
     * job trend graph
     */
    @Override
    public TrendGraph getGraph(JobsRequest request) throws Exception {
        Map<String, Object> termQuery = request.getTermQuery();
        Map<String, Object[]> rangeConditions = request.getRangeConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, rangeConditions, null, null);

        TrendGraph trendGraph = new TrendGraph();
        List<IndicatorData> data = null;
        double maxValue;
        String timeUnit;
        switch (request.getGraphType()) {
            case "cpuTrend":
                trendGraph.setName("CPU趋势");
                data = openSearchService.sumAggregationByDay(builder, request.getStart(), request.getEnd(), jobsIndex,
                        "executionDate", "vcoreSeconds");
                break;
            case "memoryTrend":
                trendGraph.setName("内存趋势");
                data = openSearchService.sumAggregationByDay(builder, request.getStart(), request.getEnd(), jobsIndex,
                        "executionDate", "memorySeconds");
                break;
            case "numTrend":
                trendGraph.setName("数量趋势");
                data = openSearchService.countDocByDay(builder, request.getStart(), request.getEnd(), jobsIndex, "executionDate");
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
    public List<Map<String, Item>> searchAppDiagnoseInfo(JobDetailRequest jobDetailRequest) throws Exception {
        Map<String, Object> termQuery = jobDetailRequest.getTermQuery();
        Map<String, SortOrder> sortQuery = jobDetailRequest.getSortConditions();
        SearchSourceBuilder builder = openSearchService.genSearchBuilder(termQuery, null, sortQuery, null);
        builder.size(1000);
        List<TaskApp> taskApps = openSearchService.find(TaskApp.class, builder, taskAppIndex + "-*");
        Map<String, Map<String, Item>> temp = new HashMap<>();
        for (TaskApp taskApp : taskApps) {
            if (taskApp.getCategories() != null && taskApp.getCategories().size() != 0) {
                List<Item> appDiagnoseItem = taskAppService.generatePartOfReport(taskApp.getApplicationId(), new HashSet<>(taskApp.getCategories()));
                for (Item item : appDiagnoseItem) {
                    Map<String, Item> appItem = temp.getOrDefault(item.getName(), new HashMap<>());
                    appItem.put(taskApp.getApplicationId(), item);
                    temp.put(item.getName(), appItem);
                }
            }
        }
        List<Map<String, Item>> res = new ArrayList<>();
        for (String category : temp.keySet()) {
            res.add(temp.get(category));
        }
        return res;
    }

    /**
     * 更新任务状态
     */
    @Override
    public void updateJobState(JobDetailRequest request) throws Exception {
        Map<String, Object> termQuery = request.getTermQuery();
        // 将在此执行周期前的job都标记为已处理
        termQuery.remove("executionDate");
        HashMap<String, Object[]> rangeQuery = new HashMap<>();
        rangeQuery.put("executionDate", new Object[]{null, DateUtil.timestampToUTCDate(request.getExecutionDate().getTime())});
        termQuery.put("taskStatus", 0);
        SearchSourceBuilder searchSourceBuilder = openSearchService.genSearchBuilder(termQuery, rangeQuery, null, null);
        BoolQueryBuilder boolQueryBuilder = (BoolQueryBuilder) searchSourceBuilder.query();
        Script script = new Script(String.format("ctx._source['taskStatus']=%d", 1));
        openSearchService.updateByQuery(boolQueryBuilder, script, jobsIndex + "-*");
        openSearchService.updateByQuery(boolQueryBuilder, script, taskAppIndex + "-*");
        String key = String.format("%s:%s:%s", request.getProjectName(), request.getFlowName(), request.getTaskName());
        redisService.set(key, String.valueOf(request.getExecutionDate().getTime()), 30 * 60);
    }

    @Override
    public JobInstance getJobInstance(String projectName, String flowName, String taskName, Date executionDate) throws Exception {
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("projectName.keyword", projectName);
        termQuery.put("flowName.keyword", flowName);
        termQuery.put("taskName.keyword", taskName);
        termQuery.put("executionDate", DateUtil.timestampToUTCDate(executionDate.getTime()));
        List<JobInstance> jobInstances = openSearchService.find(JobInstance.class, termQuery, jobIndexIndex + "-*");
        if (jobInstances.size() != 0) {
            return jobInstances.get(0);
        }
        return null;
    }

    /**
     * 生成运行耗时异常的分析结论
     */
    private String getDurationConclusion(JobAnalysis jobAnalysis) {
        String info = String.format("本任务运行耗时为%s", UnitUtil.transferRed(UnitUtil.transferSecond(jobAnalysis.getDuration() == null ? 0 : jobAnalysis.getDuration())));
        if (Strings.isNotBlank(jobAnalysis.getDurationBaseline())) {
            info = String.format("%s, 基线运行耗时为%s", info, UnitUtil.transferRed(jobAnalysis.getDurationBaseline()));
        }
        if (jobAnalysis.getCategories().contains(JobCategoryEnum.durationAbnormal.name())) {
            info = String.format("%s, 运行耗时异常", info);
        } else {
            info = String.format("%s, 未检测到异常", info);
        }
        return info;
    }

    /**
     * 获取diagnostic的诊断汇总信息
     */
    private String getDiagnosticDetectSum(List<TaskApp> taskApps) {
        List<LogInfo> yarnDetectLog = logService.getDiagnosticDetect(taskApps);
        if (yarnDetectLog.size() > 0) {
            Set<String> eventSet = new HashSet<>();
            for (LogInfo logInfo : yarnDetectLog) {
                String event = logInfo.getEvent() == null ? "其他错误" : logInfo.getEvent();
                eventSet.add(UIUtil.transferRed(event));
            }
            return String.format("从Yarn日志中检测到'%s'异常, 详细日志和诊断建议请查看异常日志分析", String.join(",", eventSet));
        }
        return null;
    }

    /**
     * 获取调度日志的诊断汇总信息
     */
    private String getSchedulerDetectSum(JobDetailRequest jobDetailRequest) {
        List<LogInfo> logInfoList = logService.getLogDetect(jobDetailRequest, "scheduler");
        if (logInfoList.size() > 0) {
            Set<String> eventSet = new HashSet<>();
            for (LogInfo logInfo : logInfoList) {
                String event = logInfo.getEvent() == null ? "其他错误" : logInfo.getEvent();
                eventSet.add(UIUtil.transferRed(event));
            }
            return String.format("从调度日志中检测到%s, 详细日志和诊断建议请查看异常日志分析", String.join(",", eventSet));
        }
        return null;
    }

    /**
     * 获取Job的异常汇总信息
     */
    private List<String> getJobAppDetectSum(List<TaskApp> taskApps) {
        Map<String, List<String>> categoryApps = new HashMap<>();
        for (TaskApp taskApp : taskApps) {
            List<String> category = taskApp.getCategories();
            if (category != null && category.size() > 0) {
                Collections.sort(category);
                String categoryCh = category.stream().map(AppCategoryEnum::getAppCategoryOfChina).map(UIUtil::transferRed).collect(Collectors.joining(","));
                List<String> appList = categoryApps.getOrDefault(categoryCh, new ArrayList<>());
                appList.add(UIUtil.transferHyperLink(String.format("/#/offline/application/detail?applicationId=%s", taskApp.getApplicationId()), taskApp.getApplicationId()));
                categoryApps.put(categoryCh, appList);
            }
        }
        List<String> categorySumApp = new ArrayList<>();
        for (String categoryStr : categoryApps.keySet()) {
            int i = 1;
            StringBuilder stringBuilder = new StringBuilder();
            List<String> appSum = categoryApps.get(categoryStr);
            for (String app : appSum) {
                if (i % 3 == 0) {
                    // 每四个进行换行
                    stringBuilder.append(app).append("<br/>");
                } else {
                    stringBuilder.append(app).append(",");
                }
                i++;
            }
            if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            categorySumApp.add(String.format("%s发生%s", stringBuilder.toString(), categoryStr));
        }
        return categorySumApp.size() == 0 ? null : categorySumApp;
    }

    /**
     * 将taskApps按执行次数进行格式化存储
     *
     * @param taskApps
     * @return
     */
    private Map<String, List<TaskAppInfo>> formatJobApps(List<TaskApp> taskApps, String schedulerType) {
        Map<String, List<TaskAppInfo>> taskAppMap = new LinkedHashMap<>();
        for (TaskApp taskApp : taskApps) {
            if (taskApp.getApplicationId() == null) {
                // appId不存在情况
                taskApp.setApplicationId("applicationId不存在");
            }
            TaskAppInfo taskAppInfo = TaskAppInfo.from(taskApp);
            taskAppInfo.setTryNumber(TryNumberUtil.updateTryNumber(taskAppInfo.getTryNumber(), schedulerType));
            String key = String.format("第%d次执行", taskAppInfo.getTryNumber() + 1);
            List<TaskAppInfo> tryNumberTaskApps = taskAppMap.getOrDefault(key, new ArrayList<>());
            // appId去重
            boolean contains = false;
            for (TaskAppInfo temp : tryNumberTaskApps) {
                if (temp.getApplicationId().equals(taskAppInfo.getApplicationId())) {
                    contains = true;
                    if (temp.getCategories() == null) {
                        temp.setCategories(taskAppInfo.getCategories());
                    }
                }
            }
            if (!contains) {
                tryNumberTaskApps.add(taskAppInfo);
            }
            taskAppMap.put(key, tryNumberTaskApps);
        }
        return taskAppMap;
    }

}
