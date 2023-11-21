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
import com.oppo.cloud.common.domain.eventlog.DetectionStorage;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.SpeculativeTaskAbnormal;
import com.oppo.cloud.common.domain.eventlog.StageDurationAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.StageDuration;
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
 * StageDuration Service
 */
@Service
public class StageDurationService extends RunTimeBaseService<StageDuration> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.STAGE_DURATION.getCategory();
    }

    @Override
    public StageDuration generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        StageDuration stageDuration = new StageDuration();
        List<StageDurationAbnormal> stageDurationAbnormalList = new ArrayList<>();
        for (JSONObject data : (List<JSONObject>) detectorResult.getData()) {
            stageDurationAbnormalList.add(data.toJavaObject(StageDurationAbnormal.class));
        }
        stageDuration.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = stageDuration.getChartList();
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildChart(chartSummary);
        chartSummary.setDes(String.format(MessageSourceUtil.get("STAGE_DURATION_CHART_DESC"), chartSummary.getUnit()));
        List<MetricInfo> metricInfoList = chartSummary.getDataList();
        List<String> jobStageId = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (StageDurationAbnormal stageDurationAbnormal : stageDurationAbnormalList) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(stageDurationAbnormal.getStageId()));
            List<ValueInfo> yValues = metricInfo.getYValues();
            // compute time
            ValueInfo yValue1 = new ValueInfo();
            yValue1.setType("compute");
            yValue1.setValue(UnitUtil.transferDouble(stageDurationAbnormal.getTaskAccDuration() / 1000.0));

            // idle time
            ValueInfo yValue2 = new ValueInfo();
            yValue2.setType("idle");
            yValue2.setValue(UnitUtil.transferDouble(stageDurationAbnormal.getStageDuration() / 1000.0
                    - stageDurationAbnormal.getTaskAccDuration() / 1000.0));
            yValues.add(yValue1);
            yValues.add(yValue2);
            metricInfoList.add(metricInfo);
            if (stageDurationAbnormal.getAbnormal()) {
                jobStageId.add(String.format("job[<span style=\"color: #e24a4a;\">%d</span>].stage[<span " +
                                "style=\"color: #e24a4a;\">%d</span>]", stageDurationAbnormal.getJobId(),
                        stageDurationAbnormal.getStageId()));
                values.add(String.format("%.2f%%", stageDurationAbnormal.getRatio()));
            }
        }
        chartList.add(chartSummary);
        stageDuration.getVars().put("jobStages", String.join(",", jobStageId));
        stageDuration.getVars().put("values", String.join(",", values));
        stageDuration.getVars().put("threshold", String.valueOf(config.getStageDurationConfig().getThreshold()));
        return stageDuration;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("STAGE_DURATION_CONCLUSION_DESC"),
                thresholdMap.getOrDefault("threshold", ""));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("STAGE_DURATION_ANALYSIS");
    }

    @Override
    public String getType() {
        return "chart";
    }

    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("duration");
        chart.setUnit("s");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("compute", new Chart.ChartInfo(MessageSourceUtil.get("STAGE_DURATION_CHART_COMPUTE"), UIUtil.NORMAL_COLOR));
        dataCategory.put("idle", new Chart.ChartInfo(MessageSourceUtil.get("STAGE_DURATION_CHART_IDLE"), UIUtil.PLAIN_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
