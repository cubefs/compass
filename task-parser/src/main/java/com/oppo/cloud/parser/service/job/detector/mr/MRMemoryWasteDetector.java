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
import com.oppo.cloud.common.constant.MRTaskType;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.mr.MRMemWasteAbnormal;
import com.oppo.cloud.common.domain.mr.MRTaskMemPeak;
import com.oppo.cloud.common.domain.mr.config.MRMemWasteConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.mr.CounterInfo;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.mr.MRTaskAttemptInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MRMemoryWasteDetector implements IDetector {

    private final DetectorParam param;

    private final MRMemWasteConfig config;

    public MRMemoryWasteDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrMemWasteConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<MRMemWasteAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_MEMORY_WASTE.getCategory(), false);

        List<MRMemWasteAbnormal> data = new ArrayList<>();
        MRAppInfo mrAppInfo = param.getMrAppInfo();
        Map<String, String> confMap = param.getMrAppInfo().getConfMap();

        long mapMemoryMB = Long.parseLong(confMap.getOrDefault(CounterInfo.MRConfiguration.MAP_MEMORY_MB.getKey(), "0"));
        long reduceMemoryMB = Long.parseLong(confMap.getOrDefault(CounterInfo.MRConfiguration.REDUCE_MEMORY_MB.getKey(), "0"));
        if (mapMemoryMB == 0 || reduceMemoryMB == 0) {
            log.error("appId：{},mapMemoryMB:{},reduceMemoryMB:{}", param.getAppId(),
                    confMap.get(CounterInfo.MRConfiguration.MAP_MEMORY_MB.getKey()),
                    confMap.get(CounterInfo.MRConfiguration.REDUCE_MEMORY_MB.getKey()));
            return detectorResult;
        }

        MRMemWasteAbnormal mapResult = calculateMemoryWaste(detectorResult, mrAppInfo.getMapList(), mapMemoryMB, MRTaskType.MAP.getName());
        MRMemWasteAbnormal reduceResult = calculateMemoryWaste(detectorResult, mrAppInfo.getReduceList(), reduceMemoryMB, MRTaskType.REDUCE.getName());
        if (detectorResult.getAbnormal()) {
            if (mapResult != null) {
                data.add(mapResult);
            }
            if (reduceResult != null) {
                data.add(reduceResult);
            }
        }
        detectorResult.setData(data);
        return detectorResult;
    }


    private MRMemWasteAbnormal calculateMemoryWaste(DetectorResult detectorResult, List<MRTaskAttemptInfo> lists,
                                                    long allocationMB, String taskType) {
        MRMemWasteAbnormal memWasteAbnormal = new MRMemWasteAbnormal();
        memWasteAbnormal.setAbnormal(false);
        memWasteAbnormal.setTaskType(taskType);
        memWasteAbnormal.setMemory(allocationMB);

        List<MRTaskMemPeak> peakList = new ArrayList<>();
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
            return null;
        }
        if (memAvailableSeconds < memComputeSeconds) {
            log.warn("appId:{}, memAvailableSeconds less than memComputeSeconds", param.getAppId());
            return null;
        }

        double wastePercent = (((double) memAvailableSeconds - memComputeSeconds) / memAvailableSeconds) * 100;
        boolean abnormal;
        if (MRTaskType.MAP.getName().equals(taskType)) {
            abnormal = wastePercent > config.getMapThreshold();
        } else {
            abnormal = wastePercent > config.getReduceThreshold();
        }
        if (abnormal) {
            detectorResult.setAbnormal(true);
        }
        memWasteAbnormal.setAbnormal(abnormal);
        memWasteAbnormal.setWastePercent(wastePercent);
        memWasteAbnormal.setTaskMemPeakList(peakList);
        return memWasteAbnormal;
    }

}
