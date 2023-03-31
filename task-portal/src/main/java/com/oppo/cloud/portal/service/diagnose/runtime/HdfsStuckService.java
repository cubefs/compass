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
import com.oppo.cloud.common.domain.eventlog.*;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.HdfsStuck;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * hdfs卡顿
 */
@Service
public class HdfsStuckService extends RunTimeBaseService<HdfsStuck> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.HDFS_STUCK.getCategory();
    }

    @Override
    public HdfsStuck generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        HdfsStuck hdfsStuck = new HdfsStuck();
        List<HdfsStuckAbnormal> hdfsStuckAbnormalList = new ArrayList<>();
        for (JSONObject data : (List<JSONObject>) detectorResult.getData()) {
            hdfsStuckAbnormalList.add(data.toJavaObject(HdfsStuckAbnormal.class));
        }
        List<Chart<MetricInfo>> chartList = hdfsStuck.getChartList();
        // Stage分布图表
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildSummaryChartInfo(chartSummary);
        chartSummary.setDes("每个Stage中任务处理数据速率的中位值和最小值的比值的分布图");
        List<MetricInfo> metricSummaryList = chartSummary.getDataList();
        List<String> info = new ArrayList<>();
        for (HdfsStuckAbnormal hdfsStuckAbnormal : hdfsStuckAbnormalList) {
            MetricInfo metricSummary = new MetricInfo();
            metricSummary.setXValue(String.valueOf(hdfsStuckAbnormal.getStageId()));
            List<ValueInfo> ySummaryValues = metricSummary.getYValues();
            if (hdfsStuckAbnormal.getAbnormal()) {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(hdfsStuckAbnormal.getRatio()), "abnormal"));
            } else {
                ySummaryValues.add(new ValueInfo(UnitUtil.transferDouble(hdfsStuckAbnormal.getRatio()), "normal"));
            }
            metricSummaryList.add(metricSummary);
            // Task分布图表
            if (hdfsStuckAbnormal.getGraphs() != null) {
                chartList.add(buildTaskChart(hdfsStuckAbnormal, info));
            }
        }
        chartList.add(0, chartSummary);
        hdfsStuck.getVars().put("hdfsSlowInfo", String.join(",", info));
        hdfsStuck.getVars().put("threshold", String.format("%.2f", config.getHdfsStuckConfig().getThreshold()));
        return hdfsStuck;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("计算Stage中每个任务的处理速率(读取数据量与耗时的比值), 当处理速率的中位值与最小值的比大于%s,即判定为HDFS卡顿",
                thresholdMap.getOrDefault("threshold", "10"));
    }

    @Override
    public String generateItemDesc() {
        return "HDFS卡顿分析";
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
        chart.setY("inputSize/duration");
        chart.setUnit("MB/s");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("min", new Chart.ChartInfo("最小值", UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo("中位值", UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo("正常值", UIUtil.NORMAL_COLOR));

        chart.setDataCategory(dataCategory);
    }

    /**
     * 构建Task分布图
     */
    private Chart<MetricInfo> buildTaskChart(HdfsStuckAbnormal hdfsStuckAbnormal, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);
        // 补充图表信息
        chart.setDes(String.format("Job[%s] Stage[%s]每个task读取数据量与耗时比值的分布情况(%s)", hdfsStuckAbnormal.getJobId(),
                hdfsStuckAbnormal.getStageId(), chart.getUnit()));
        long taskId = 0;
        long jobId = 0;
        long stageId = 0;
        double min = 0;
        double median = 0;
        List<MetricInfo> metricInfoList = chart.getDataList();
        for (HdfsStuckGraph hdfsStuckGraph : hdfsStuckAbnormal.getGraphs()) {
            double value = UnitUtil.transferDouble(hdfsStuckGraph.getPercent());
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(hdfsStuckGraph.getTaskId()));
            List<ValueInfo> yValues = metricInfo.getYValues();
            ValueInfo yValue = new ValueInfo();
            yValue.setValue(value);
            yValue.setType(hdfsStuckGraph.getGraphType());
            yValues.add(yValue);
            metricInfoList.add(metricInfo);
            if ("min".equals(hdfsStuckGraph.getGraphType())) {
                taskId = hdfsStuckGraph.getTaskId();
                stageId = hdfsStuckAbnormal.getStageId();
                jobId = hdfsStuckAbnormal.getJobId();
                min = hdfsStuckGraph.getPercent();
            }
            if ("median".equals(hdfsStuckGraph.getGraphType())) {
                median = hdfsStuckGraph.getPercent();
            }
        }
        info.add(String.format("job[<span style=\"color: #e24a4a;\">%d</span>].stage[<span style=\"color: " +
                "#e24a4a;\">%d</span>].task[<span style=\"color: #e24a4a;\">%d</span>]处理速率为<span style=\"color: #e24a4a;\">%.2f</span>MB/s 中位值为%.2fMB/s",
                jobId,
                stageId, taskId, min, median));
        return chart;
    }

    /**
     * 补充汇总图表信息
     */
    private void buildSummaryChartInfo(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("median/min");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo("数据正常Stage", UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo("数据卡顿Stage", UIUtil.ABNORMAL_COLOR));

        chart.setDataCategory(dataCategory);
    }
}
