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
import com.oppo.cloud.common.domain.eventlog.HdfsStuckAbnormal;
import com.oppo.cloud.common.domain.eventlog.JobDurationAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.JobDuration;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * job耗时异常
 */
@Service
public class JobDurationService extends RunTimeBaseService<JobDuration> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.JOB_DURATION.getCategory();
    }

    @Override
    public JobDuration generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        JobDuration jobDuration = new JobDuration();
        List<JobDurationAbnormal> jobDurationAbnormalList = new ArrayList<>();
        for (JSONObject data : (List<JSONObject>) detectorResult.getData()) {
            jobDurationAbnormalList.add(data.toJavaObject(JobDurationAbnormal.class));
        }
        jobDuration.setAbnormal(detectorResult.getAbnormal());
        List<Chart<MetricInfo>> chartList = jobDuration.getChartList();
        Chart<MetricInfo> chartSummary = new Chart<>();
        buildChart(chartSummary);
        chartSummary.setDes(String.format("每个Job计算-空闲时间分布(%s)", chartSummary.getUnit()));
        List<MetricInfo> metricInfoList = chartSummary.getDataList();
        List<String> jobIds = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (JobDurationAbnormal jobDurationAbnormal : jobDurationAbnormalList) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(jobDurationAbnormal.getJobId()));
            List<ValueInfo> yValues = metricInfo.getYValues();

            // 空闲时间
            ValueInfo yValue1 = new ValueInfo();
            yValue1.setValue(UnitUtil.transferDouble(jobDurationAbnormal.getJobDuration() / 1000.0
                    - jobDurationAbnormal.getStageAccDuration() / 1000.0));
            yValue1.setType("idle");

            // 计算时间
            ValueInfo yValue2 = new ValueInfo();
            yValue2.setValue(UnitUtil.transferDouble(jobDurationAbnormal.getStageAccDuration() / 1000.0));
            yValue2.setType("compute");
            yValues.add(yValue2);
            yValues.add(yValue1);
            metricInfoList.add(metricInfo);
            // 判断是否异常
            if (jobDurationAbnormal.getAbnormal()) {
                values.add(String.format("%.2f%%", jobDurationAbnormal.getRatio()));
                jobIds.add(String.format("job[<span style=\"color: #e24a4a;\">%d</span>]",
                        jobDurationAbnormal.getJobId()));
            }
        }
        chartList.add(chartSummary);
        jobDuration.getVars().put("jobs", String.join(",", jobIds));
        jobDuration.getVars().put("values", String.join(",", values));
        jobDuration.getVars().put("threshold", String.format("%.2f%%", config.getJobDurationConfig().getThreshold()));
        return jobDuration;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("Job中空闲时间 (job总时间 - stage累计时间) 与总时间的占比超过%s%%，即判定为Job耗时异常",
                thresholdMap.getOrDefault("threshold", ""));
    }

    @Override
    public String generateItemDesc() {
        return "Job耗时异常分析";
    }

    @Override
    public String getType() {
        return "chart";
    }

    private void buildChart(Chart<MetricInfo> chart) {
        chart.setX("job id");
        chart.setY("duration");
        chart.setUnit("s");
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("compute", new Chart.ChartInfo("job计算时间", UIUtil.NORMAL_COLOR));
        dataCategory.put("idle", new Chart.ChartInfo("job空闲时间", UIUtil.PLAIN_COLOR));
        chart.setDataCategory(dataCategory);
    }
}
