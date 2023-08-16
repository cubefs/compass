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

package com.oppo.cloud.parser.service.job.detector.spark;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.MemWasteAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.MemWasteConfig;
import com.oppo.cloud.common.domain.gc.ExecutorPeakMemory;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.parser.domain.job.MemoryCalculateParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public class MemWasteDetector {

    private final MemWasteConfig config;

    public MemWasteDetector(MemWasteConfig config) {
        this.config = config;
    }

    public DetectorResult detect(List<GCReport> gcReports, MemoryCalculateParam memoryCalculateParam) {
        DetectorResult<MemWasteAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MEMORY_WASTE.getCategory(), false);
        long appTotalTime = memoryCalculateParam.getAppTotalTime();
        MemWasteAbnormal memWastedAbnormal = new MemWasteAbnormal();
        memWastedAbnormal.setAbnormal(false);
        memWastedAbnormal.setTotalTime(appTotalTime);
        memWastedAbnormal.setDriverMemory(memoryCalculateParam.getDriverMemory());
        memWastedAbnormal.setExecutorMemory(memoryCalculateParam.getExecutorMemory());

        Map<String, Long> executorRuntimeMap = memoryCalculateParam.getExecutorRuntimeMap();

        long mb = 1024 * 1024;
        long totalMemoryTime = memWastedAbnormal.getDriverMemory() / mb * appTotalTime;

        long totalMemoryTimeComputeMemoryTime = 0L;
        for (GCReport gcReport : gcReports) {
            if (gcReport.getExecutorId() == 0) {
                totalMemoryTimeComputeMemoryTime +=
                        gcReport.getMaxHeapUsedSize() / 1024 * memWastedAbnormal.getTotalTime();
            } else {
                Long executorTime = executorRuntimeMap.get(String.valueOf(gcReport.getExecutorId()));
                if (executorTime != null) {
                    totalMemoryTimeComputeMemoryTime += gcReport.getMaxHeapUsedSize() / 1024 * executorTime;
                    totalMemoryTime += memWastedAbnormal.getExecutorMemory() / mb * executorTime;
                } else {
                    log.error("cannotGetExecutorTime:{},{}", gcReport.getApplicationId(), gcReport.getExecutorId());
                }
            }
        }
        memWastedAbnormal.setTotalMemoryTime(totalMemoryTime);
        memWastedAbnormal.setTotalMemoryComputeTime(totalMemoryTimeComputeMemoryTime);
        float wastePercent = ((float) (totalMemoryTime - totalMemoryTimeComputeMemoryTime) / totalMemoryTime) * 100;
        memWastedAbnormal.setWastePercent(wastePercent);
        if (wastePercent > this.config.getThreshold()
                && memoryCalculateParam.getAppTotalTime() > this.config.getDuration()) {
            memWastedAbnormal.setAbnormal(true);
            detectorResult.setAbnormal(true);
        }
        memWastedAbnormal.setThreshold(this.config.getThreshold());

        gcReports.sort(Comparator.comparing(GCReport::getMaxHeapUsedSize));
        if (gcReports.size() > 10) {
            List<GCReport> results = new ArrayList<>();
            GCReport driverGc = gcReports.stream().filter(gc -> gc.getExecutorId() == 0).findFirst().orElse(null);
            List<GCReport> executorGcs = gcReports.subList(gcReports.size() - 9, gcReports.size());
            if (driverGc != null) {
                results.add(driverGc);
            }
            results.addAll(executorGcs);
            memWastedAbnormal.setGcReportList(results);
        } else {
            memWastedAbnormal.setGcReportList(gcReports);
        }

        List<ExecutorPeakMemory> executorPeakMemoryList = new ArrayList<>();
        for (GCReport gcReport : gcReports) {
            executorPeakMemoryList.add(new ExecutorPeakMemory(gcReport.getExecutorId(), gcReport.getMaxHeapUsedSize(),
                    gcReport.getLogPath()));
        }

        memWastedAbnormal.setExecutorPeakMemoryList(executorPeakMemoryList);
        detectorResult.setData(memWastedAbnormal);
        return detectorResult;
    }
}
