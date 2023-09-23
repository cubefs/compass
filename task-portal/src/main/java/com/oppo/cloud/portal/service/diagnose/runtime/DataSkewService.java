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
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据倾斜
 */
@Service
public class DataSkewService extends RunTimeBaseService<DataSkew> {

    /**
     * 获取该子类异常类型
     */
    @Override
    public String getCategory() {
        return AppCategoryEnum.DATA_SKEW.getCategory();
    }

    /**
     * 产生该异常类型的报告
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
        // Stage图表信息
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildSummaryChartInfo(chartSummary);
        chartSummary.setDes("Stage中任务Shuffle Read Records最大值与中位值比值的分布图");
        List<MetricInfo> metricSummaryList = chartSummary.getDataList();
        List<String> info = new ArrayList<>();
        for (DataSkewAbnormal dataSkewTask : dataSkewAbnormalList) {
            MetricInfo metricSummary = new MetricInfo();
            metricSummary.setXValue(String.valueOf(dataSkewTask.getStageId()));
            List<ValueInfo> ySummaryValues = metricSummary.getYValues();
            if (dataSkewTask.getAbnormal()) {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(dataSkewTask.getRatio()), "abnormal"));
            } else {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(dataSkewTask.getRatio()), "normal"));
            }
            metricSummaryList.add(metricSummary);
            // 异常Stage的task图表信息
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
        return String.format("数据倾斜诊断规则如下: <br/> " +
                "&nbsp;  1、任务总耗时>30min<br/>" +
                "&nbsp;  2、stage耗时/任务总耗时>45%%<br/>" +
                "&nbsp;  3、shuffle read的数据量满足一下条件之一：<br> " +
                "&nbsp;&nbsp;    a、当5万<中位值<=10万，且最大值/中位值>=100 <br>" +
                "&nbsp;&nbsp;    b、当10万<中位值<100万,且最大值/中位值>=50<br/>" +
                "&nbsp;&nbsp;    c、当100万<中位值<500万, 且最大值/中位值>=10<br/>" +
                "&nbsp;&nbsp;    d、当500万<中位值<2000万, 且最大值/中位值>=5<br/>" +
                "&nbsp;&nbsp;    e、当2000万<中位值<3000万, 且最大值/中位值>=3.5<br/>" +
                "&nbsp;&nbsp;    f、当3000万<中位值<4000万, 且最大值/中位值>=3<br/>" +
                "&nbsp;&nbsp;    g、当4000万<中位值<5000万, 且最大值/中位值>=2.25<br/>" +
                "&nbsp;&nbsp;    h、当5000万<中位值, 且最大值/中位值>=2<br/>");
    }

    @Override
    public String generateItemDesc() {
        return "数据倾斜分析";
    }

    @Override
    public String getType() {
        return "chart";
    }

    /**
     * 补充图表信息
     */
    private void buildChartInfo(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY("shuffle_read");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("max", new Chart.ChartInfo("最大值", UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo("中位值", UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo("正常值", UIUtil.NORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }

    /**
     * 构建Task分布图
     */
    private Chart<MetricInfo> buildTaskChart(DataSkewAbnormal dataSkewTask, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);
        // 补充图表信息
        chart.setDes(String.format("Stage[%s]Reduce任务Shuffle Read Records",
                dataSkewTask.getStageId()));
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
        info.add(String.format(
                "job[<span style=\"color: #e24a4a;\">%s</span>].stage[<span style=\"color: #e24a4a;\">%s</span>].task[<span style=\"color: #e24a4a;\">%s</span>]shuffle read的数据量为<span style=\"color: #e24a4a;\">%s</span> 中位值为%s",
                dataSkewTask.getJobId(), dataSkewTask.getStageId(),
                taskId, UnitUtil.transferRows(max), UnitUtil.transferRows(median)));
        return chart;
    }

    /**
     * 补充汇总图表信息
     */
    private void buildSummaryChartInfo(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("max/median");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo("数据正常Stage", UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo("数据倾斜Stage", UIUtil.ABNORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
