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
import com.oppo.cloud.common.constant.MRTaskType;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.mr.MRDataSkewAbnormal;
import com.oppo.cloud.common.domain.mr.MRDataSkewGraph;
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
 * MR数据倾斜
 */
@Service
public class MRDataSkewService extends RunTimeBaseService<DataSkew> {

    /**
     * 获取该子类异常类型
     */
    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_DATA_SKEW.getCategory();
    }

    /**
     * 产生该异常类型的报告
     */
    @Override
    public DataSkew generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        DataSkew dataSkew = new DataSkew();
        dataSkew.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = dataSkew.getChartList();
        List<String> info = new ArrayList<>();

        MRDataSkewAbnormal data = ((JSONObject) detectorResult.getData()).toJavaObject(MRDataSkewAbnormal.class);
        if (data.getMapGraphList() != null) {
            chartList.add(buildTaskChart(MRTaskType.MAP.getName(), data.getMapGraphList(), info));
        }
        if (data.getReduceGraphList() != null) {
            chartList.add(buildTaskChart(MRTaskType.REDUCE.getName(), data.getReduceGraphList(), info));
        }

        dataSkew.getVars().put("dataSkewInfo", String.join(",", info));
        return dataSkew;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("");
    }

    @Override
    public String generateItemDesc() {
        return "MR数据倾斜分析";
    }

    @Override
    public String getType() {
        return "chart";
    }


    private void buildChartInfo(Chart<MetricInfo> chart) {
        chart.setX("task id");
        chart.setY("BYTES");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("max", new Chart.ChartInfo("最大值", UIUtil.ABNORMAL_COLOR));
        dataCategory.put("median", new Chart.ChartInfo("中位值", UIUtil.KEY_COLOR));
        dataCategory.put("normal", new Chart.ChartInfo("正常值", UIUtil.NORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }

    private Chart<MetricInfo> buildTaskChart(String taskType, List<MRDataSkewGraph> graphList, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);
        chart.setDes(String.format("%s任务处理数据量分布图", taskType));
        List<MetricInfo> metricInfoList = chart.getDataList();
        double max = 0.0;
        double median = 0.0;
        String taskId = "";
        for (MRDataSkewGraph dataSkewGraph : graphList) {
            double value = UnitUtil.transferDouble(dataSkewGraph.getDataSize());
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
                "task[<span style=\"color: #e24a4a;\">%s</span>]处理数据量为<span style=\"color: #e24a4a;\">%s</span> 中位值为%s",
                taskId, UnitUtil.transferByte(max), UnitUtil.transferByte(median)));
        return chart;
    }
}
