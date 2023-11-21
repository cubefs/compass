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
import com.oppo.cloud.common.domain.eventlog.DataSkewAbnormal;
import com.oppo.cloud.common.domain.eventlog.DataSkewGraph;
import com.oppo.cloud.common.domain.eventlog.DetectionStorage;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.DataSkew;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataSkew Service
 */
@Service
public class DataSkewService extends RunTimeBaseService<DataSkew> {


    @Override
    public String getCategory() {
        return AppCategoryEnum.DATA_SKEW.getCategory();
    }

    /**
     * generate report data
     */
    @Override
    public DataSkew generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        DataSkew dataSkew = new DataSkew();
        List<DataSkewAbnormal> dataSkewAbnormalList = new ArrayList<>();
        for (JSONObject data : (List<JSONObject>) detectorResult.getData()) {
            dataSkewAbnormalList.add(data.toJavaObject(DataSkewAbnormal.class));
        }
        dataSkew.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = dataSkew.getChartList();
        // Stage chart
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildSummaryChartInfo(chartSummary);
        chartSummary.setDes(MessageSourceUtil.get("DATA_SKEW_CHART_DESC"));
        List<MetricInfo> metricSummaryList = chartSummary.getDataList();
        List<String> info = new ArrayList<>();
        for (DataSkewAbnormal dataSkewTask : dataSkewAbnormalList) {
            if (dataSkewTask.getRatio() == null) {
                continue;
            }
            MetricInfo metricSummary = new MetricInfo();
            metricSummary.setXValue(String.valueOf(dataSkewTask.getStageId()));
            List<ValueInfo> ySummaryValues = metricSummary.getYValues();
            if (dataSkewTask.getAbnormal()) {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(dataSkewTask.getRatio()), "abnormal"));
            } else {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(dataSkewTask.getRatio()), "normal"));
            }
            metricSummaryList.add(metricSummary);
            // Chart information for tasks in the abnormal stage.
            if (dataSkewTask.getDataSkewGraphs() != null) {
                chartList.add(buildTaskChart(dataSkewTask, info));
            }
        }
        if (chartList.size() == 0) {
            return null;
        }
        chartList.add(0, chartSummary);
        dataSkew.getVars().put("dataSkewInfo", String.join(",", info));
        return dataSkew;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("DATA_SKEW_CONCLUSION_DESC"));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("DATA_SKEW_ANALYSIS");
    }

    @Override
    public String getType() {
        return "chart";
    }

    /**
     * build chart information
     */
    private void buildChartInfo(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY("shuffle_read");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("max", new Chart.ChartInfo(MessageSourceUtil.get("DATA_SKEW_CHART_MAX"), UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo(MessageSourceUtil.get("DATA_SKEW_CHART_MEDIAN"), UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("DATA_SKEW_CHART_NORMAL"), UIUtil.NORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }

    /**
     * build task chart
     */
    private Chart<MetricInfo> buildTaskChart(DataSkewAbnormal dataSkewTask, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);

        chart.setDes(String.format(MessageSourceUtil.get("DATA_SKEW_CHART_STAGE_DESC"), dataSkewTask.getStageId()));
        List<MetricInfo> metricInfoList = chart.getDataList();
        double max = 0.0;
        double median = 0.0;
        String taskId = "";
        for (DataSkewGraph dataSkewGraph : dataSkewTask.getDataSkewGraphs()) {
            double value = UnitUtil.transferDouble(dataSkewGraph.getTotalRecordsRead());
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(dataSkewGraph.getTaskId()));
            List<ValueInfo> yValues = metricInfo.getYValues();
            ValueInfo yValue = new ValueInfo();
            yValue.setValue(value);
            yValue.setType(dataSkewGraph.getGraphType());
            yValues.add(yValue);
            metricInfoList.add(metricInfo);
            if ("max".equals(dataSkewGraph.getGraphType())) {
                max = value;
                taskId = String.valueOf(dataSkewGraph.getTaskId());
            }
            if ("median".equals(dataSkewGraph.getGraphType())) {
                median = value;
            }
        }
        info.add(String.format(MessageSourceUtil.get("DATA_SKEW_CONCLUSION_INFO"),
                dataSkewTask.getJobId(), dataSkewTask.getStageId(),
                taskId, UnitUtil.transferRows(max), UnitUtil.transferRows(median)));
        return chart;
    }

    /**
     * build summary chart information
     */
    private void buildSummaryChartInfo(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("max/median");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("DATA_SKEW_CHART_INFO_NORMAL"), UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo(MessageSourceUtil.get("DATA_SKEW_CHART_INFO_ABNORMAL"), UIUtil.ABNORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
