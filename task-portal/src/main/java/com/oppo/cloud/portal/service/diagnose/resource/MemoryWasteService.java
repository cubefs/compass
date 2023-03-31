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

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectionStorage;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.MemWasteAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.gc.ExecutorPeakMemory;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.domain.gc.MemoryAnalyze;
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
 * 内存资源浪费
 */
@Order(2)
@Service
@Slf4j
public class MemoryWasteService extends ResourceBaseService<MemoryWaste> {

    @Value(value = "${custom.elasticsearch.gcIndex.name}")
    String gcIndex;

    @Override
    public String getCategory() {
        return AppCategoryEnum.MEMORY_WASTE.getCategory();
    }

    @Override
    public String getType() {
        return "memoryChart";
    }

    @Override
    public MemoryWaste generateData(DetectorResult detectorResult, DetectorConfig config,
                                    String applicationId) throws Exception {
        MemoryWaste memoryWaste = new MemoryWaste();
        // 获取es中driver、executor的时间
        MemWasteAbnormal memWasteAbnormal =
                ((JSONObject) detectorResult.getData()).toJavaObject(MemWasteAbnormal.class);
        if (memWasteAbnormal == null) {
            return null;
        }
        // 获取gc报告(包括executor、driver峰值内存以及相应的gc报告数据)
        List<GCReport> gcReportList = memWasteAbnormal.getGcReportList();
        if (gcReportList.size() == 0) {
            return null;
        }
        // executor、driver峰值内存
        List<ExecutorPeakMemory> executorPeakMemoryList = memWasteAbnormal.getExecutorPeakMemoryList();
        // executor、driver峰值内存图表数据
        Chart<MetricInfo> chart = this.getChart(executorPeakMemoryList, memWasteAbnormal, memoryWaste.getVars());
        // GC报告列表
        List<MemoryWaste.ComputeNode> computeNodeList = getGcLogList(gcReportList);
        if (computeNodeList.size() > 1) {
            // 说明有Executor,则显示分配的内存
            memoryWaste.getVars().put("executorMemory",
                    String.format("%.2fGB", UnitUtil.transferBToGB((memWasteAbnormal.getExecutorMemory()))));
        } else {
            memoryWaste.getVars().put("executorMemory", String.format("%.2fGB", 0.0));
        }
        memoryWaste.getChartList().add(chart);
        memoryWaste.setComputeNodeList(computeNodeList);
        // 计算内存浪费
        memoryWaste.setAbnormal(memWasteAbnormal.getAbnormal());
        Float wasteRatio = memWasteAbnormal.getWastePercent();
        memoryWaste.getVars().put("wasteRatio", String.format("%.2f%%", wasteRatio == null ? 0 : wasteRatio));
        memoryWaste.getVars().put("driverMemory",
                String.format("%.2fGB", UnitUtil.transferBToGB((memWasteAbnormal.getDriverMemory()))));
        memoryWaste.getVars().put("threshold", String.format("%.2f%%",
                memWasteAbnormal.getThreshold() == null ? 0 : config.getMemWasteConfig().getThreshold()));
        return memoryWaste;
    }

    @Override
    public String generateConclusionDesc(IsAbnormal data) {
        return String.format(
                "内存浪费计算规则:<br/> &nbsp;  总内存时间 = executor配置内存大小 * executor数量 * app运行时间 <br/> &nbsp;  执行消耗内存时间 = sum(executor峰值内存 * executor执行时间) <br/>"
                        +
                        "&nbsp;  浪费内存的百分比 = (总内存时间-执行消耗内存时间)/总内存时间" +
                        "<br/>&nbsp;  当内存浪费占比超过%s, 即判断发生内存浪费",
                data.getVars().get("threshold"));
    }

    @Override
    public String generateItemDesc() {
        return "内存浪费分析";
    }

    /**
     * 生成GC分析列表
     *
     * @param gcReportList
     * @return
     */
    private List<MemoryWaste.ComputeNode> getGcLogList(List<GCReport> gcReportList) {
        List<MemoryWaste.ComputeNode> computeNodeList = new ArrayList<>();
        // 去重
        Set<Integer> check = new HashSet<>();
        for (GCReport gcReport : gcReportList) {
            if (check.add(gcReport.getExecutorId())) {
                MemoryWaste.ComputeNode computeNode = new MemoryWaste.ComputeNode();
                computeNode.setExecutorId(gcReport.getExecutorId());
                computeNode.setNodeType(gcReport.getLogType());
                String hostName = "";
                if (gcReport.getLogPath() != null && !"".equals(gcReport.getLogPath())) {
                    String[] temp = gcReport.getLogPath().split("/");
                    hostName = temp[temp.length - 1];
                }
                computeNode.setHostName(hostName);
                if ("driver".equals(gcReport.getLogType())) {
                    computeNodeList.add(0, computeNode);
                } else {
                    computeNodeList.add(computeNode);
                }
            }
        }
        return computeNodeList;
    }

    /**
     * 根据内存数据生成图表数据
     */
    private Chart<MetricInfo> getChart(List<ExecutorPeakMemory> executorPeakMemories, MemWasteAbnormal memWasteAbnormal,
                                       Map<String, String> vars) {
        Chart<MetricInfo> chart = new Chart<>();
        buildChartInfo(chart);
        List<MetricInfo> metricInfoList = chart.getDataList();
        long executorPeakUsed = 0;
        long driverPeakUsed = 0;
        for (ExecutorPeakMemory executorPeakMemory : executorPeakMemories) {
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(String.valueOf(executorPeakMemory.getExecutorId()));
            List<ValueInfo> valueInfoList = metricInfo.getYValues();
            valueInfoList.add(new ValueInfo(UnitUtil.transferKBToGB((long) executorPeakMemory.getPeakUsed()), "peak"));
            if (executorPeakMemory.getExecutorId() == 0) {
                metricInfo.setXValue("driver");
                valueInfoList.add(new ValueInfo(
                        UnitUtil.transferKBToGB(
                                (memWasteAbnormal.getDriverMemory() / 1024 - executorPeakMemory.getPeakUsed())),
                        "free"));
                driverPeakUsed = executorPeakMemory.getPeakUsed();
                metricInfoList.add(0, metricInfo);
            } else {
                valueInfoList.add(new ValueInfo(
                        UnitUtil.transferKBToGB(
                                memWasteAbnormal.getExecutorMemory() / 1024 - executorPeakMemory.getPeakUsed()),
                        "free"));
                executorPeakUsed = Math.max(executorPeakUsed, executorPeakMemory.getPeakUsed());
                metricInfoList.add(metricInfo);
            }

        }
        vars.put("executorPeak", String.format("%.2fGB", UnitUtil.transferKBToGB((executorPeakUsed))));
        vars.put("driverPeak", String.format("%.2fGB", UnitUtil.transferKBToGB((driverPeakUsed))));
        return chart;
    }

    /**
     * 查询es获取GC报告数据
     */
    private List<GCReport> getGcReport(String applicationId) throws Exception {
        HashMap<String, Object> termQueryConditions = new HashMap<>(1);
        termQueryConditions.put("applicationId.keyword", applicationId);
        return elasticSearchService.find(GCReport.class, termQueryConditions, gcIndex + "-*");
    }

    /**
     * 查询es获取内存分析报告(driver和executor数据分开存放)
     *
     * @param applicationId
     * @return
     */
    private List<MemoryAnalyze> getMemoryAnalyze(String applicationId) throws Exception {
        HashMap<String, Object> termQueryConditions = new HashMap<>(1);
        termQueryConditions.put("applicationId.keyword", applicationId);
        List<MemoryAnalyze> memoryAnalyzeList =
                elasticSearchService.find(MemoryAnalyze.class, termQueryConditions, gcIndex + "-*");
        if (memoryAnalyzeList.size() != 2) {
            log.error("get getMemoryAnalyze from es abnormal, applicationId:{}", applicationId);
        }
        return memoryAnalyzeList;
    }

    /**
     * 补充图表信息
     *
     * @param chart
     */
    private void buildChartInfo(Chart<MetricInfo> chart) {
        chart.setDes("每个executor的峰值内存和最大内存分布图");
        chart.setUnit("GB");
        chart.setX("executor id");
        chart.setY("内存");

        Map<String, Chart.ChartInfo> dataCategory = new HashMap<>(2);
        dataCategory.put("free", new Chart.ChartInfo("空闲内存", UIUtil.PLAIN_COLOR));
        dataCategory.put("peak", new Chart.ChartInfo("峰值内存", UIUtil.KEY_COLOR));

        chart.setDataCategory(dataCategory);
    }

    /**
     * 获取driver、executor内存浪费分析数据
     */
    private MemWasteAbnormal getDurationInfo(String applicationId) throws Exception {
        HashMap<String, Object> termQueryConditions = new HashMap<>();
        termQueryConditions.put("applicationId.keyword", applicationId);
        termQueryConditions.put("appCategory.keyword", AppCategoryEnum.MEMORY_WASTE.getCategory());
        // es查询元数据
        List<DetectionStorage> detectionStorageList =
                elasticSearchService.find(DetectionStorage.class, termQueryConditions, detectIndex);
        if (detectionStorageList.size() != 0) {
            DetectionStorage detectionStorage = detectionStorageList.get(0);
            return detectionStorage.getMemWasteAbnormal();
        }
        return null;
    }
}
