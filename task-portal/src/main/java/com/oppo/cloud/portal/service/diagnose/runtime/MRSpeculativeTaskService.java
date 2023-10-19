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
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.mr.MRSpeculativeAbnormal;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.SpeculativeTask;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MRSpeculativeTask Service
 */
@Order(3)
@Service
public class MRSpeculativeTaskService extends RunTimeBaseService<SpeculativeTask> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_SPECULATIVE_TASK.getCategory();
    }

    @Override
    public SpeculativeTask generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        SpeculativeTask speculativeTask = new SpeculativeTask();
        MRSpeculativeAbnormal mrSpeculativeAbnormal = ((JSONObject) detectorResult.getData()).toJavaObject(MRSpeculativeAbnormal.class);
        speculativeTask.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = speculativeTask.getChartList();
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildChart(chartSummary);
        chartSummary.setDes("每个推测执行任务耗时分布");
        List<MetricInfo> metricInfoList = chartSummary.getDataList();
        long maxElapsedTime = 0L;
        String attemptId = "";
        int size = mrSpeculativeAbnormal.getTaskAttemptIds().size();
        for (int i = 0; i < size; i++) {
            String x = mrSpeculativeAbnormal.getTaskAttemptIds().get(i);
            Long y = mrSpeculativeAbnormal.getElapsedTime().get(i);
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(x);
            List<ValueInfo> yValues = metricInfo.getYValues();
            ValueInfo yValue = new ValueInfo();
            yValue.setValue(y);
            yValue.setType("normal");
            yValues.add(yValue);
            metricInfoList.add(metricInfo);
            if (y > maxElapsedTime) {
                attemptId = x;
                maxElapsedTime = y;
            }
        }
        chartList.add(chartSummary);
        speculativeTask.getVars().put("values", String.valueOf(size));
        speculativeTask.getVars().put("attemptId", attemptId);
        speculativeTask.getVars().put("maxElapsedTime", DateUtil.timeSimplify((double) (maxElapsedTime / 1000)));
        speculativeTask.getVars().put("threshold", String.valueOf(config.getMrSpeculativeTaskConfig().getThreshold()));
        return speculativeTask;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("MapReduce中推测执行任务数超过%s个", thresholdMap.getOrDefault("threshold", "30"));
    }

    @Override
    public String generateItemDesc() {
        return "MR推测执行过多分析";
    }

    @Override
    public String getType() {
        return "chart";
    }

    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("id");
        chart.setY("推测执行耗时");
        chart.setUnit("ms");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("normal", new Chart.ChartInfo("推测执行耗时", UIUtil.NORMAL_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
