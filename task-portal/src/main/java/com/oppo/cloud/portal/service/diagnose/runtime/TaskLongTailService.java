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

package com.oppo.cloud.portal.service.diagnose.runtime;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.*;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.TaskLongTail;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务长尾异常
 */
@Slf4j
@Service
public class TaskLongTailService extends RunTimeBaseService<TaskLongTail> {

    @Value(value = "${custom.elasticsearch.appIndex.name}")
    String appIndex;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Override
    public String getCategory() {
        return AppCategoryEnum.TASK_DURATION.getCategory();
    }

    @Override
    public TaskLongTail generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        TaskLongTail taskLongTail = new TaskLongTail();
        List<TaskDurationAbnormal> taskDurationAbnormalList = new ArrayList<>();
        for (JSONObject data : (List<JSONObject>) detectorResult.getData()) {
            taskDurationAbnormalList.add(data.toJavaObject(TaskDurationAbnormal.class));
        }
        taskLongTail.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = taskLongTail.getChartList();
        // Stage分布图表
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildSummaryChartInfo(chartSummary);
        chartSummary.setDes("每个Stage 任务运行耗时最大值与中位值比值的分布图");
        List<MetricInfo> metricSummaryList = chartSummary.getDataList();
        List<String> info = new ArrayList<>();
        for (TaskDurationAbnormal taskDurationAbnormal : taskDurationAbnormalList) {
            MetricInfo metricSummary = new MetricInfo();
            metricSummary.setXValue(String.valueOf(taskDurationAbnormal.getStageId()));
            List<ValueInfo> ySummaryValues = metricSummary.getYValues();
            if (taskDurationAbnormal.getAbnormal()) {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(taskDurationAbnormal.getRatio()), "abnormal"));
            } else {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(taskDurationAbnormal.getRatio()), "normal"));
            }
            metricSummaryList.add(metricSummary);
            if (taskDurationAbnormal.getGraphs() != null) {
                chartList.add(buildTaskChart(taskDurationAbnormal, info));
            }
        }
        chartList.add(0, chartSummary);
        taskLongTail.getVars().put("taskDurationInfo", String.join(" ,", info));
        // String reason = getReason(detectorResult.getApplicationId());
        // taskLongTail.getVars().put("reason", reason);
        taskLongTail.getVars().put("threshold", String.valueOf(config.getTaskDurationConfig().getThreshold()));

        return taskLongTail;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("Stage中任务运行耗时的最大值与中位值的比值大于%s，即判定为Stage耗时异常", thresholdMap.getOrDefault("threshold", ""));
    }

    @Override
    public String generateItemDesc() {
        return "长尾Task分析";
    }

    @Override
    public String getType() {
        return "chart";
    }

    /**
     * 生成任务图表信息
     */
    private Chart<MetricInfo> buildTaskChart(TaskDurationAbnormal taskDurationAbnormal, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChart(chart);
        chart.setDes(String.format("Stage[%s]每个Task耗时分布(%s)", taskDurationAbnormal.getStageId(), chart.getUnit()));
        List<MetricInfo> metricInfoList = chart.getDataList();
        double max = 0;
        long taskId = 0;
        double median = 0;
        for (TaskDurationGraph taskDurationGraph : taskDurationAbnormal.getGraphs()) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(taskDurationGraph.getTaskId()));
            List<ValueInfo> valueInfoList = metricInfo.getYValues();
            valueInfoList.add(new ValueInfo(UnitUtil.transferDouble(taskDurationGraph.getDuration() / 1000),
                    taskDurationGraph.getGraphType()));
            metricInfoList.add(metricInfo);
            if ("max".equals(taskDurationGraph.getGraphType())) {
                max = taskDurationGraph.getDuration();
                taskId = taskDurationGraph.getTaskId();
            }
            if ("median".equals(taskDurationGraph.getGraphType())) {
                median = taskDurationGraph.getDuration();
            }
        }
        info.add(String.format("job[<span style=\"color: #e24a4a;\">%d</span>].stage[<span style=\"color: #e24a4a;" +
                "\">%d</span>].task[<span style=\"color: #e24a4a;\">%d</span>]运行耗时<span style=\"color: #e24a4a;\">%s</span> 中位值为%s",
                taskDurationAbnormal.getJobId(),
                taskDurationAbnormal.getStageId(), taskId, DateUtil.timeSimplify(max / 1000),
                DateUtil.timeSimplify(median / 1000)));
        return chart;
    }

    /**
     * 补充Task图表信息
     */
    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY("duration");
        chart.setUnit("s");

        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("max", new Chart.ChartInfo("最大值", UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo("中位值", UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo("正常值", UIUtil.NORMAL_COLOR));

        chart.setDataCategory(dataCategory);
    }

    /**
     * 补充汇总图表信息
     */
    private void buildSummaryChartInfo(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("max/median");
        chart.setUnit("");

        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo("正常Stage", UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo("长尾Stage", UIUtil.ABNORMAL_COLOR));

        chart.setDataCategory(dataCategory);
    }

    /**
     * 判断任务是否发生数据倾斜或HDFS卡顿
     */
    private String getReason(String applicationId) {

        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId", applicationId);
        List<String> categories = new ArrayList<>();
        try {
            List<TaskApp> taskAppEsList = elasticSearchService.find(TaskApp.class, termQuery, appIndex);
            if (taskAppEsList.size() > 0) {
                categories = taskAppEsList.get(0).getCategories();
            }
        } catch (Exception e) {
            log.error("search taskAppEs failed, msg:{}", e.getMessage());
        }
        List<String> res = new ArrayList<>();
        if (categories.contains(AppCategoryEnum.DATA_SKEW.getCategory())) {
            res.add(AppCategoryEnum.DATA_SKEW.getDesc());
        }
        if (categories.contains(AppCategoryEnum.HDFS_STUCK.getCategory())) {
            res.add(AppCategoryEnum.HDFS_STUCK.getDesc());
        }
        if (res.size() == 0) {
            return "";
        } else {
            return String.format("同时检测出该任务发生<span style=\"color: #e24a4a;\">%s</span>, 可能是此原因导致了Task耗时异常。",
                    String.join(",", res));
        }
    }
}
