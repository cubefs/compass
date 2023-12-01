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
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.mr.MRDataSkewAbnormal;
import com.oppo.cloud.common.domain.mr.MRDataSkewGraph;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.DataSkew;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MRDataSkew Service
 */
@Order(1)
@Service
public class MRDataSkewService extends RunTimeBaseService<DataSkew> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_DATA_SKEW.getCategory();
    }

    @Override
    public DataSkew generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        DataSkew dataSkew = new DataSkew();
        dataSkew.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = dataSkew.getChartList();
        List<String> info = new ArrayList<>();
        List<MRDataSkewAbnormal> data = ((JSONArray) detectorResult.getData()).toJavaList(MRDataSkewAbnormal.class);
        if (data == null) {
            return null;
        }
        List<String> taskType = new ArrayList<>();
        for (MRDataSkewAbnormal dataSkewAbnormal : data) {
            if (dataSkewAbnormal.getGraphList().size() == 0) {
                continue;
            }
            chartList.add(buildTaskChart(dataSkewAbnormal, info));
            if (dataSkewAbnormal.getAbnormal()) {
                taskType.add(dataSkewAbnormal.getTaskType());
            }
        }
        dataSkew.getVars().put("dataSkewInfo", String.join("; ", info));
        dataSkew.getVars().put("taskType", String.join(",", taskType));
        dataSkew.getVars().put("taskSize", String.valueOf(config.getMrDataSkewConfig().getTaskSize()));
        dataSkew.getVars().put("taskDuration", String.valueOf(config.getMrDataSkewConfig().getTaskDuration()));
        dataSkew.getVars().put("mapThreshold", String.valueOf(config.getMrDataSkewConfig().getMapThreshold()));
        dataSkew.getVars().put("reduceThreshold", String.valueOf(config.getMrDataSkewConfig().getReduceThreshold()));

        return dataSkew;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("MR_DATA_SKEW_CONCLUSION_DESC"),
                thresholdMap.get("taskSize"), thresholdMap.get("taskDuration"), thresholdMap.get("mapThreshold"),
                thresholdMap.get("reduceThreshold"));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("MR_DATA_SKEW_ANALYSIS");
    }

    @Override
    public String getType() {
        return "chart";
    }


    private void buildChartInfo(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY(MessageSourceUtil.get("MR_DATA_SKEW_CHART_Y"));
        chart.setUnit("MB");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(4);
        dataCategory.put("max", new Chart.ChartInfo(MessageSourceUtil.get("MR_DATA_SKEW_CHART_MAX"), UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo(MessageSourceUtil.get("MR_DATA_SKEW_CHART_MEDIAN"), UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("MR_DATA_SKEW_CHART_NORMAL"), UIUtil.NORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }

    private Chart<MetricInfo> buildTaskChart(MRDataSkewAbnormal dataSkewAbnormal, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);
        chart.setDes(String.format(MessageSourceUtil.get("MR_DATA_SKEW_CHART_TASK_DESC"), dataSkewAbnormal.getTaskType()));
        List<MetricInfo> metricInfoList = chart.getDataList();
        double max = 0.0;
        double median = 0.0;
        String taskId = "";
        for (MRDataSkewGraph dataSkewGraph : dataSkewAbnormal.getGraphList()) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(dataSkewGraph.getTaskId()));
            List<ValueInfo> yValues = metricInfo.getYValues();
            ValueInfo yValue = new ValueInfo();
            yValue.setValue(UnitUtil.transferBToMB(dataSkewGraph.getDataSize()));
            yValue.setType(dataSkewGraph.getGraphType());
            yValues.add(yValue);
            metricInfoList.add(metricInfo);
            if ("max".equals(dataSkewGraph.getGraphType())) {
                max = dataSkewGraph.getDataSize();
                taskId = String.valueOf(dataSkewGraph.getTaskId());
            }
            if ("median".equals(dataSkewGraph.getGraphType())) {
                median = dataSkewGraph.getDataSize();
            }
        }
        info.add(String.format(MessageSourceUtil.get("MR_DATA_SKEW_CONCLUSION_INFO"),
                dataSkewAbnormal.getTaskType(), taskId, UnitUtil.transferByte(max),
                DateUtil.timeSimplify((double) (dataSkewAbnormal.getElapsedTime() / 1000)), UnitUtil.transferByte(median)));
        return chart;
    }
}
