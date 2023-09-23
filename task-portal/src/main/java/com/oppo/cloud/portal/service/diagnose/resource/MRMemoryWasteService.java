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

package com.oppo.cloud.portal.service.diagnose.resource;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.MRTaskType;
import com.oppo.cloud.common.domain.eventlog.DetectionStorage;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.MemWasteAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.gc.ExecutorPeakMemory;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.domain.gc.MemoryAnalyze;
import com.oppo.cloud.common.domain.mr.MRMemWasteAbnormal;
import com.oppo.cloud.common.domain.mr.MRTaskMemPeak;
import com.oppo.cloud.common.util.ui.UIUtil;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.resources.MemoryWaste;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MR内存资源浪费
 */
@Order(3)
@Service
@Slf4j
public class MRMemoryWasteService extends ResourceBaseService<MemoryWaste> {


    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_MEMORY_WASTE.getCategory();
    }

    @Override
    public String getType() {
        return "memoryChart";
    }

    @Override
    public MemoryWaste generateData(DetectorResult detectorResult, DetectorConfig config, String applicationId) throws Exception {
        MemoryWaste memoryWaste = new MemoryWaste();
        memoryWaste.setAbnormal(detectorResult.getAbnormal());

        List<MRMemWasteAbnormal> data = ((JSONArray) detectorResult.getData()).toJavaList(MRMemWasteAbnormal.class);
        if (data == null) {
            return null;
        }
        List<String> info = new ArrayList<>();
        for (MRMemWasteAbnormal memWasteAbnormal : data) {
            if (memWasteAbnormal.getTaskMemPeakList().size() == 0) {
                continue;
            }
            Chart<MetricInfo> chart = this.getMapReduceChart(memWasteAbnormal, info);
            if (chart.getDataList().size() > 0) {
                memoryWaste.getChartList().add(chart);
            }
        }
        memoryWaste.getVars().put("memoryWaste", String.join("; ", info));
        memoryWaste.getVars().put("mapThreshold", String.format("%.2f%%", config.getMrMemWasteConfig().getMapThreshold()));
        memoryWaste.getVars().put("reduceThreshold", String.format("%.2f%%", config.getMrMemWasteConfig().getReduceThreshold()));

        return memoryWaste;
    }

    @Override
    public String generateConclusionDesc(IsAbnormal data) {
        return String.format(
                "内存浪费计算规则:<br/> &nbsp;  总内存时间 = sum(map/reduce配置内存大小 * map/reduce运行时间) <br/> &nbsp;  执行消耗内存时间 = sum(map/reduce峰值内存 * map/reduce执行时间) <br/>"
                        +
                        "&nbsp;  浪费内存的百分比 = (总内存时间-执行消耗内存时间)/总内存时间" +
                        "<br/>&nbsp;  当map内存浪费占比超过%s或reduce内存浪费占比超过%s, 即判断发生内存浪费",
                data.getVars().get("mapThreshold"), data.getVars().get("reduceThreshold"));
    }

    @Override
    public String generateItemDesc() {
        return "MR内存浪费分析";
    }


    private Chart<MetricInfo> getMapReduceChart(MRMemWasteAbnormal memWasteAbnormal, List<String> info) {
        Chart<MetricInfo> chart = new Chart<>();
        buildMapReduceChartInfo(chart, memWasteAbnormal.getTaskType());

        List<MetricInfo> metricInfoList = chart.getDataList();
        long executorPeakUsed = 0;

        for (MRTaskMemPeak mrTaskMemPeak : memWasteAbnormal.getTaskMemPeakList()) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(mrTaskMemPeak.getTaskId()));
            List<ValueInfo> valueInfoList = metricInfo.getYValues();
            valueInfoList.add(new ValueInfo(UnitUtil.transferMBToGB((long) mrTaskMemPeak.getPeakUsed()), "peak"));
            valueInfoList.add(new ValueInfo(UnitUtil.transferMBToGB(memWasteAbnormal.getMemory() - mrTaskMemPeak.getPeakUsed()), "free"));
            executorPeakUsed = Math.max(executorPeakUsed, mrTaskMemPeak.getPeakUsed());
            metricInfoList.add(metricInfo);
        }
        info.add(String.format(
                "%s分配内存<span style=\"color: #e24a4a;\">%s</span>, 峰值内存<span style=\"color: #e24a4a;\">%s</span>, " +
                        "内存浪费为<span style=\"color: #e24a4a;\">%s</span>",
                memWasteAbnormal.getTaskType(), String.format("%.2fGB", UnitUtil.transferMBToGB((memWasteAbnormal.getMemory()))),
                        String.format("%.2fGB", UnitUtil.transferMBToGB((executorPeakUsed))),
                        String.format("%.2f%%",  memWasteAbnormal.getWastePercent())));

        return chart;
    }

    private void buildMapReduceChartInfo(Chart<MetricInfo> chart, String taskType) {
        chart.setDes(String.format("%s任务的峰值内存和最大内存分布图", taskType));
        chart.setUnit("GB");
        chart.setX("task id");
        chart.setY("内存");

        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("free", new Chart.ChartInfo("空闲内存", UIUtil.PLAIN_COLOR));
        dataCategory.put("peak", new Chart.ChartInfo("峰值内存", UIUtil.KEY_COLOR));

        chart.setDataCategory(dataCategory);
    }

}
