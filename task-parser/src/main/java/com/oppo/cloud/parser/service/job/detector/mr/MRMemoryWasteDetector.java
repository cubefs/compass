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

package com.oppo.cloud.parser.service.job.detector.mr;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.mr.MRTaskMemPeak;
import com.oppo.cloud.common.domain.mr.MRMemWasteAbnormal;
import com.oppo.cloud.common.domain.mr.config.MRMemWasteConfig;
import com.oppo.cloud.parser.domain.job.MRDetectorParam;
import com.oppo.cloud.parser.domain.mr.CounterInfo;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.mr.MRTaskAttemptInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MRMemoryWasteDetector implements IDetector {

    private final MRDetectorParam param;

    private final MRMemWasteConfig config;

    MRMemoryWasteDetector(MRDetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrMemWasteConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<MRMemWasteAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_MEMORY_WASTE.getCategory(), false);

        MRMemWasteAbnormal memWastedAbnormal = new MRMemWasteAbnormal();
        memWastedAbnormal.setAbnormal(false);

        MRAppInfo mrAppInfo = param.getMrAppInfo();
        Map<String, String> confMap = param.getMrAppInfo().getConfMap();

        long mapMemoryMB = Long.parseLong(confMap.get(CounterInfo.MRConfiguration.MAP_MEMORY_MB.getKey()));
        long reduceMemoryMB = Long.parseLong(confMap.get(CounterInfo.MRConfiguration.REDUCE_MEMORY_MB.getKey()));

        List<MRTaskMemPeak> mapPeakList = new ArrayList<>();
        List<MRTaskMemPeak> reducePeakList = new ArrayList<>();
        double mapWastePercent = calculateMemoryWaste(mrAppInfo.getMapList(), mapMemoryMB, mapPeakList);
        double reduceWastePercent = calculateMemoryWaste(mrAppInfo.getReduceList(), reduceMemoryMB, reducePeakList);

        memWastedAbnormal.setMapMemory(mapMemoryMB);
        memWastedAbnormal.setReduceMemory(mapMemoryMB);
        memWastedAbnormal.setMapWastePercent(mapWastePercent);
        memWastedAbnormal.setReduceWastePercent(reduceWastePercent);
        memWastedAbnormal.setMapTaskMemPeakList(mapPeakList);
        memWastedAbnormal.setReduceTaskMemPeakList(reducePeakList);

        if (mapWastePercent > config.getMapThreshold() || reduceWastePercent > config.getReduceThreshold()) {
            memWastedAbnormal.setAbnormal(true);
            detectorResult.setAbnormal(true);
        }

        detectorResult.setData(memWastedAbnormal);
        return detectorResult;
    }


    private double calculateMemoryWaste(List<MRTaskAttemptInfo> lists, long allocationMB, List<MRTaskMemPeak> peakList) {
        long memComputeSeconds = 0L;
        long memAvailableSeconds = 0L;

        for (MRTaskAttemptInfo task : lists) {
            String group = CounterInfo.CounterGroupName.TASK_COUNTER.getCounterGroupName();
            Map<String, Long> metrics = task.getCounters().get(group);
            long memMBValue = metrics.get(CounterInfo.CounterName.PHYSICAL_MEMORY_BYTES.getCounterName()) / 1024 / 1024;
            long elapsedSecondsValue = task.getElapsedTime() / 1000;
            memComputeSeconds += memMBValue * elapsedSecondsValue;
            memAvailableSeconds += allocationMB * elapsedSecondsValue;

            peakList.add(new MRTaskMemPeak(task.getTaskId(), (int) memMBValue));
        }

        if (memComputeSeconds == 0) {
            return 0F;
        }

        return (((double) memAvailableSeconds - memComputeSeconds) / memAvailableSeconds) * 100;
    }

}
