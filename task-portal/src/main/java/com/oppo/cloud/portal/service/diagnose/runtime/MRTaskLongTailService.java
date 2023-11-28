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

import com.alibaba.fastjson2.JSONArray;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.TaskDurationGraph;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.mr.MRTaskDurationAbnormal;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.TaskLongTail;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MRTaskLongTail Service
 */
@Order(2)
@Slf4j
@Service
public class MRTaskLongTailService extends RunTimeBaseService<TaskLongTail> {


    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_TASK_DURATION.getCategory();
    }

    @Override
    public TaskLongTail generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        TaskLongTail taskLongTail = new TaskLongTail();
        taskLongTail.setAbnormal(detectorResult.getAbnormal());
        List<MRTaskDurationAbnormal> data = ((JSONArray) detectorResult.getData()).toJavaList(MRTaskDurationAbnormal.class);
        if (data == null) {
            return null;
        }
        List<Chart<MetricInfo>> chartList = taskLongTail.getChartList();
        List<String> info = new ArrayList<>();
        List<String> taskType = new ArrayList<>();
        for (MRTaskDurationAbnormal taskDurationAbnormal : data) {
            if (taskDurationAbnormal.getGraphList().size() == 0) {
                continue;
            }
            Chart<MetricInfo> chart = buildTaskChart(taskDurationAbnormal.getTaskType(), taskDurationAbnormal.getGraphList(), info);
            if (chart.getDataList().size() > 0) {
                chartList.add(chart);
            }
            if (taskDurationAbnormal.getAbnormal()) {
                taskType.add(taskDurationAbnormal.getTaskType());
            }
        }
        taskLongTail.getVars().put("taskDurationInfo", String.join(" ,", info));
        taskLongTail.getVars().put("taskType", String.join(",", taskType));
        taskLongTail.getVars().put("taskDuration", String.valueOf(config.getMrTaskDurationConfig().getDuration()));
        taskLongTail.getVars().put("mapThreshold", String.valueOf(config.getMrTaskDurationConfig().getMapThreshold()));
        taskLongTail.getVars().put("reduceThreshold", String.valueOf(config.getMrTaskDurationConfig().getReduceThreshold()));
        return taskLongTail;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("MR_LONG_TAIL_CONCLUSION_DESC"),
                thresholdMap.get("taskDuration"), thresholdMap.get("mapThreshold"), thresholdMap.get("reduceThreshold"));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("MR_LONG_TAIL_ANALYSIS");
    }

    @Override
    public String getType() {
        return "chart";
    }


    private void buildChartInfo(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY(MessageSourceUtil.get("MR_LONG_TAIL_CHART_Y"));
        chart.setUnit("ms");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(1);
        dataCategory.put("max", new Chart.ChartInfo(MessageSourceUtil.get("MR_LONG_TAIL_CHART_MAX"), UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo(MessageSourceUtil.get("MR_LONG_TAIL_CHART_MEDIAN"), UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("MR_LONG_TAIL_CHART_NORMAL"), UIUtil.NORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }

    private Chart<MetricInfo> buildTaskChart(String taskType, List<TaskDurationGraph> graphList, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);
        chart.setDes(String.format(MessageSourceUtil.get("MR_LONG_TAIL_CHART_DESC"), taskType));
        List<MetricInfo> metricInfoList = chart.getDataList();
        double max = 0.0;
        double median = 0.0;
        String taskId = "";
        for (TaskDurationGraph taskDurationGraph : graphList) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(taskDurationGraph.getTaskId()));
            List<ValueInfo> yValues = metricInfo.getYValues();
            ValueInfo yValue = new ValueInfo();
            yValue.setValue(taskDurationGraph.getDuration());
            yValue.setType(taskDurationGraph.getGraphType());
            yValues.add(yValue);
            metricInfoList.add(metricInfo);
            if ("max".equals(taskDurationGraph.getGraphType())) {
                max = taskDurationGraph.getDuration();
                taskId = String.valueOf(taskDurationGraph.getTaskId());
            }
            if ("median".equals(taskDurationGraph.getGraphType())) {
                median = taskDurationGraph.getDuration();
            }
        }
        info.add(String.format(MessageSourceUtil.get("MR_LONG_TAIL_CONCLUSION_INFO"),
                taskType, taskId, DateUtil.timeSimplify(max / 1000), DateUtil.timeSimplify(median / 1000)));
        return chart;
    }

}
