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
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.*;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.TaskLongTail;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.service.OpenSearchService;
import com.oppo.cloud.portal.util.MessageSourceUtil;
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
 * TaskLongTail Service
 */
@Slf4j
@Service
public class TaskLongTailService extends RunTimeBaseService<TaskLongTail> {

    @Value(value = "${custom.opensearch.appIndex.name}")
    String appIndex;

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
        // Stage chart
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildSummaryChartInfo(chartSummary);
        chartSummary.setDes(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_STAGE_DESC"));
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
        taskLongTail.getVars().put("threshold", String.valueOf(config.getTaskDurationConfig().getThreshold()));

        return taskLongTail;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("TASK_LONG_TAIL_CONCLUSION_DESC"), thresholdMap.getOrDefault("threshold", ""));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("TASK_LONG_TAIL_ANALYSIS");
    }

    @Override
    public String getType() {
        return "chart";
    }

    /**
     * build task chart
     */
    private Chart<MetricInfo> buildTaskChart(TaskDurationAbnormal taskDurationAbnormal, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChart(chart);
        chart.setDes(String.format(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_TASK_DESC"), taskDurationAbnormal.getStageId(), chart.getUnit()));
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
        info.add(String.format(MessageSourceUtil.get("TASK_LONG_TAIL_CONCLUSION_INFO"),
                taskDurationAbnormal.getJobId(),
                taskDurationAbnormal.getStageId(), taskId, DateUtil.timeSimplify(max / 1000),
                DateUtil.timeSimplify(median / 1000)));
        return chart;
    }

    /**
     * build chart information
     */
    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY("duration");
        chart.setUnit("s");

        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("max", new Chart.ChartInfo(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_MAX"), UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_MEDIAN"), UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_NORMAL"), UIUtil.NORMAL_COLOR));

        chart.setDataCategory(dataCategory);
    }

    /**
     * build summary chart information
     */
    private void buildSummaryChartInfo(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("max/median");
        chart.setUnit("");

        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_STAGE_NORMAL"), UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo(MessageSourceUtil.get("TASK_LONG_TAIL_CHART_STAGE_ABNORMAL"), UIUtil.ABNORMAL_COLOR));

        chart.setDataCategory(dataCategory);
    }

}
