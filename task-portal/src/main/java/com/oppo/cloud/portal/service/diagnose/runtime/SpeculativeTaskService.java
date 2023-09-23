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
        chartSummary.setDes("每个Stage推测执行数量分布");
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
            // 判断是否异常
            if (speculativeTaskAbnormal.getAbnormal()) {
                yValue.setType("abnormal");
                values.add(String.format("%d个", speculativeTaskAbnormal.getSpeculativeCount()));
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
        return String.format("Stage中推测执行任务数超过%s个，即可判定为推测执行过多", thresholdMap.getOrDefault("threshold", "30"));
    }

    @Override
    public String generateItemDesc() {
        return "推测执行过多分析";
    }

    @Override
    public String getType() {
        return "chart";
    }

    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("stage id");
        chart.setY("推测执行数量");
        chart.setUnit("");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo("正常stage", UIUtil.NORMAL_COLOR));
        dataCategory.put("abnormal", new Chart.ChartInfo("异常stage", UIUtil.ABNORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
