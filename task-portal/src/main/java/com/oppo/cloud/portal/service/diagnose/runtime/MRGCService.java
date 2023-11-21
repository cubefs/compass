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
import com.oppo.cloud.common.constant.MRTaskType;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.mr.MRGCGraph;
import com.oppo.cloud.common.domain.mr.MRGCAbnormal;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.mr.MRGC;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MR GC
 */
@Order(3)
@Service
@Slf4j
public class MRGCService extends RunTimeBaseService<MRGC> {
    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_GC_ABNORMAL.getCategory();
    }

    @Override
    public String getType() {
        return "chart";
    }

    @Override
    public MRGC generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        MRGC mrGC = new MRGC();
        List<MRGCAbnormal> gcData = ((JSONArray) detectorResult.getData()).toJavaList(MRGCAbnormal.class);
        if (gcData == null) {
            return null;
        }
        List<String> info = new ArrayList<>();
        for (MRGCAbnormal gcAbnormal : gcData) {
            if (gcAbnormal.getGraphList() != null && gcAbnormal.getGraphList().size() > 0) {
                Chart<MetricInfo> chart = this.getMapReduceChart(gcAbnormal, info, config);
                mrGC.getChartList().add(chart);
            }
            if (gcAbnormal.getAbnormal()) {
                mrGC.setAbnormal(true);
            }
        }
        mrGC.getVars().put("mrGCAbnormal", String.join("; ", info));
        mrGC.getVars().put("mapThreshold", String.valueOf(config.getMrGCConfig().getMapThreshold()));
        mrGC.getVars().put("reduceThreshold", String.valueOf(config.getMrGCConfig().getReduceThreshold()));
        return mrGC;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format(MessageSourceUtil.get("MR_GC_CONCLUSION_DESC"),
                thresholdMap.get("mapThreshold"), thresholdMap.get("reduceThreshold"));
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("MR_GC_ANALYSIS");
    }

    private Chart<MetricInfo> getMapReduceChart(MRGCAbnormal gcAbnormal, List<String> info, DetectorConfig config) {
        Chart<MetricInfo> chart = new Chart<>();
        buildMapReduceChartInfo(chart, gcAbnormal.getTaskType());
        List<MetricInfo> metricInfoList = chart.getDataList();

        for (MRGCGraph graph : gcAbnormal.getGraphList()) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(graph.getTaskId()));
            List<ValueInfo> valueInfoList = metricInfo.getYValues();
            valueInfoList.add(new ValueInfo(graph.getGcTime(), "gcTime"));
            valueInfoList.add(new ValueInfo(graph.getCpuTime(), "cpuTime"));
            metricInfoList.add(metricInfo);
        }
        if (gcAbnormal.getAbnormal()) {
            Double threshold;
            if (MRTaskType.MAP.getName().equals(gcAbnormal.getTaskType())) {
                threshold = config.getMrGCConfig().getMapThreshold();
            } else {
                threshold = config.getMrGCConfig().getReduceThreshold();
            }
            info.add(String.format(
                    MessageSourceUtil.get("MR_GC_CONCLUSION_INFO"),
                    gcAbnormal.getTaskType(), UnitUtil.transferDouble(gcAbnormal.getGcTimeMedian()),
                    UnitUtil.transferDouble(gcAbnormal.getCpuTimeMedian()),
                    UnitUtil.transferDouble(gcAbnormal.getRatio()), threshold));
        }
        return chart;
    }

    private void buildMapReduceChartInfo(Chart<MetricInfo> chart, String taskType) {
        chart.setDes(String.format(MessageSourceUtil.get("MR_GC_CHART_DESC"), taskType));
        chart.setUnit("ms");
        chart.setX("task id");
        chart.setY(MessageSourceUtil.get("MR_GC_CHART_Y"));
        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("gcTime", new Chart.ChartInfo(MessageSourceUtil.get("MR_GC_CHART_GC_TIME"), UIUtil.ABNORMAL_COLOR));
        dataCategory.put("cpuTime", new Chart.ChartInfo(MessageSourceUtil.get("MR_GC_CHART_CPU_TIME"), UIUtil.KEY_COLOR));
        chart.setDataCategory(dataCategory);
    }

}
