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

package com.oppo.cloud.parser.service.job.detector;

import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.config.*;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DetectorManager {

    private final DetectorParam param;

    private final DetectorConfig config;

    public DetectorManager(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig();
    }

    private List<IDetector> createOneClickDetectors() {
        List<IDetector> detectors = new ArrayList<>();
        detectors.add(new CpuWasteDetector(param));
        detectors.add(new DataSkewDetector(param));
        detectors.add(new GlobalSortDetector(param));
        detectors.add(new HdfsStuckDetector(param));
        detectors.add(new JobDurationDetector(param));
        detectors.add(new LargeTableScanDetector(param));
        detectors.add(new OOMWarnDetector(param));
        detectors.add(new SpeculativeTaskDetector(param));
        detectors.add(new StageDurationDetector(param));
        detectors.add(new TaskDurationDetector(param));
        return detectors;
    }

    private List<IDetector> createDetectors() {
        List<IDetector> detectors = new ArrayList<>();

        CpuWasteConfig cpuWasteConfig = this.config.getCpuWasteConfig();
        if (!cpuWasteConfig.getDisable() && !durationFilter(cpuWasteConfig.getDuration())) {
            detectors.add(new CpuWasteDetector(param));
        }

        DataSkewConfig dataSkewConfig = this.config.getDataSkewConfig();
        if (!dataSkewConfig.getDisable() && !durationFilter(dataSkewConfig.getDuration())) {
            detectors.add(new DataSkewDetector(param));
        }

        GlobalSortConfig globalSortConfig = this.config.getGlobalSortConfig();
        if (!globalSortConfig.getDisable()) {
            detectors.add(new GlobalSortDetector(param));
        }

        HdfsStuckConfig hdfsStuckConfig = this.config.getHdfsStuckConfig();
        if (!hdfsStuckConfig.getDisable()) {
            detectors.add(new HdfsStuckDetector(param));
        }

        JobDurationConfig jobDurationConfig = this.config.getJobDurationConfig();
        if (!jobDurationConfig.getDisable()) {
            detectors.add(new JobDurationDetector(param));
        }

        LargeTableScanConfig largeTableScanConfig = this.config.getLargeTableScanConfig();
        if (!largeTableScanConfig.getDisable() && !durationFilter(largeTableScanConfig.getDuration())) {
            detectors.add(new LargeTableScanDetector(param));
        }

        OOMWarnConfig oomWarnConfig = this.config.getOomWarnConfig();
        if (!oomWarnConfig.getDisable() && !durationFilter(oomWarnConfig.getDuration())) {
            detectors.add(new OOMWarnDetector(param));
        }

        SpeculativeTaskConfig speculativeTaskConfig = this.config.getSpeculativeTaskConfig();
        if (!speculativeTaskConfig.getDisable() && !durationFilter(speculativeTaskConfig.getDuration())) {
            detectors.add(new SpeculativeTaskDetector(param));
        }

        StageDurationConfig stageDurationConfig = this.config.getStageDurationConfig();
        if (!stageDurationConfig.getDisable()) {
            detectors.add(new StageDurationDetector(param));
        }

        TaskDurationConfig taskDurationConfig = this.config.getTaskDurationConfig();
        if (!this.config.getTaskDurationConfig().getDisable() && !durationFilter(taskDurationConfig.getDuration())) {
            detectors.add(new TaskDurationDetector(param));
        }

        return detectors;
    }

    private boolean durationFilter(Long duration) {
        return this.param.getAppDuration() < duration;
    }

    public DetectorStorage run() {
        List<IDetector> detectors;
        if (this.param.isOneClick()) {
            detectors = createOneClickDetectors();
        } else {
            detectors = createDetectors();
        }

        DetectorStorage detectorStorage = new DetectorStorage(
                this.param.getFlowName(), this.param.getProjectName(),
                this.param.getTaskName(), this.param.getExecutionTime(),
                this.param.getTryNumber(), this.param.getAppId(),
                this.param.getLogPath(), this.config);

        for (IDetector detector : detectors) {
            DetectorResult result;
            try {
                result = detector.detect();
            } catch (Exception e) {
                log.error("Exception:", e);
                continue;
            }
            if (result == null) {
                continue;
            }
            if (result.getAbnormal()) {
                detectorStorage.setAbnormal(result.getAbnormal());
                log.info("DetectorResult:{},{}", this.param.getAppId(), result.getAppCategory());
            }
            detectorStorage.addDetectorResult(result);
        }

        return detectorStorage;
    }

}
