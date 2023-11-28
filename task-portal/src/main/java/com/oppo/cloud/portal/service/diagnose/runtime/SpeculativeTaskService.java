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
import com.oppo.cloud.portal.domain.diagnose.runtime.SpeculativeTask;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpeculativeTaskService extends RunTimeBaseService<SpeculativeTask> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.SPECULATIVE_TASK.getCategory();
    }

    @Override
    public SpeculativeTask generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        SpeculativeTask speculativeTask = new SpeculativeTask();
        List<SpeculativeTaskAbnormal> speculativeTaskAbnormalList = new ArrayList<>();
        for (JSONObject data : (List<JSONObject>) detectorResult.getData()) {
            speculativeTaskAbnormalList.add(data.toJavaObject(SpeculativeTaskAbnormal.class));
        }
        speculativeTask.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = speculativeTask.getChartList();
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildChart(chartSummary);
        chartSummary.setDes(MessageSourceUtil.get("SPECULATIVE_TASK_CHART_DESC"));
        List<MetricInfo> metricInfoList = chartSummary.getDataList();
        List<String> jobStages = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (SpeculativeTaskAbnormal speculativeTaskAbnormal : speculativeTaskAbnormalList) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(speculativeTaskAbnormal.getStageId()));
            List<ValueInfo> yValues = metricInfo.getYValues();
            ValueInfo yValue = new ValueInfo();
            yValue.setValue(speculativeTaskAbnormal.getSpeculativeCount());
            yValue.setType("normal");
            // judge abnormal
            if (speculativeTaskAbnormal.getAbnormal()) {
                yValue.setType("abnormal");
                values.add(String.format("%d", speculativeTaskAbnormal.getSpeculativeCount()));
                jobStages.add(String.format("job[<span style=\"color: #e24a4a;\">%d</span>].stage[<span style=\"color" +
                                ": #e24a4a;\">%d</span>]", speculativeTaskAbnormal.getJobId(),
                        speculativeTaskAbnormal.getStageId()));
            }
            yValues.add(yValue);
            metricInfoList.add(metricInfo);
        }
        chartList.add(chartSummary);
        speculativeTask.getVars().put("jobStages", String.join(",", jobStages));
        speculativeTask.getVars().put("values", String.join(",", values));
        speculativeTask.getVars().put("threshold", String.valueOf(config.getSpeculativeTaskConfig().getThreshold()));
        return speculativeTask;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("SPECULATIVE_TASK_CONCLUSION_DESC"), thresholdMap.getOrDefault("threshold", "30"));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("SPECULATIVE_TASK_ANALYSIS");
    }

    @Override
    public String getType() {
        return "chart";
    }

    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY(MessageSourceUtil.get("SPECULATIVE_TASK_CHART_Y"));
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo(MessageSourceUtil.get("SPECULATIVE_TASK_CHART_NORMAL"), UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo(MessageSourceUtil.get("SPECULATIVE_TASK_CHART_ABNORMAL"), UIUtil.ABNORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
