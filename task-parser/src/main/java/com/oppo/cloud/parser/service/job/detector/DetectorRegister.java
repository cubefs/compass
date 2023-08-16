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

import com.oppo.cloud.common.domain.eventlog.config.*;
import com.oppo.cloud.common.domain.mr.config.*;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.service.job.detector.mr.*;
import com.oppo.cloud.parser.service.job.detector.spark.*;

import java.util.ArrayList;
import java.util.List;

public class DetectorRegister {

    private final DetectorParam param;

    private final DetectorConfig config;

    private List<IDetector> detectors;

    public DetectorRegister(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig();
        detectors = new ArrayList<>();
    }

    public List<IDetector> registerDetectors() {
        switch (this.param.getAppType()) {
            case SPARK:
                registerSparkDetectors();
                break;
            case MAPREDUCE:
                registerMapReduceDetectors();
                break;
            default:
                break;
        }
        return this.detectors;
    }

    private void registerSparkDetectors() {
        registerCpuWasteDetector();
        registerDataSkewDetector();
        registerGlobalSortDetector();
        registerHdfsStuckDetector();
        registerJobDurationDetector();
        registerLargeTableScanDetector();
        registerOOMWarnDetector();
        registerSpeculativeTaskDetector();
        registerStageDurationDetector();
        registerTaskDurationDetector();
    }

    private void registerMapReduceDetectors() {
        registerMRLargeTableScanDetector();
        registerMRMemoryWasteDetector();
        registerMRDataSkewDetector();
        registerMRSpeculativeTaskDetector();
        registerMRTaskDurationDetector();
        registerMRGCDetector();
    }

    private void registerCpuWasteDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new CpuWasteDetector(this.param));
            return;
        }
        CpuWasteConfig cpuWasteConfig = this.config.getCpuWasteConfig();
        if (!cpuWasteConfig.getDisable() && durationFilter(cpuWasteConfig.getDuration())) {
            this.detectors.add(new CpuWasteDetector(this.param));
        }
    }

    private void registerDataSkewDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new DataSkewDetector(this.param));
            return;
        }
        DataSkewConfig dataSkewConfig = this.config.getDataSkewConfig();
        if (!dataSkewConfig.getDisable() && durationFilter(dataSkewConfig.getDuration())) {
            this.detectors.add(new DataSkewDetector(this.param));
        }
    }

    private void registerGlobalSortDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new GlobalSortDetector(this.param));
            return;
        }
        GlobalSortConfig globalSortConfig = this.config.getGlobalSortConfig();
        if (!globalSortConfig.getDisable()) {
            this.detectors.add(new GlobalSortDetector(this.param));
        }
    }

    private void registerHdfsStuckDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new HdfsStuckDetector(this.param));
            return;
        }
        HdfsStuckConfig hdfsStuckConfig = this.config.getHdfsStuckConfig();
        if (!hdfsStuckConfig.getDisable()) {
            this.detectors.add(new HdfsStuckDetector(this.param));
        }
    }

    private void registerJobDurationDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new JobDurationDetector(this.param));
            return;
        }
        JobDurationConfig jobDurationConfig = this.config.getJobDurationConfig();
        if (!jobDurationConfig.getDisable()) {
            this.detectors.add(new JobDurationDetector(this.param));
        }
    }

    private void registerLargeTableScanDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new LargeTableScanDetector(this.param));
            return;
        }
        LargeTableScanConfig largeTableScanConfig = this.config.getLargeTableScanConfig();
        if (!largeTableScanConfig.getDisable() && durationFilter(largeTableScanConfig.getDuration())) {
            this.detectors.add(new LargeTableScanDetector(this.param));
        }
    }

    private void registerOOMWarnDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new OOMWarnDetector(this.param));
            return;
        }
        OOMWarnConfig oomWarnConfig = this.config.getOomWarnConfig();
        if (!oomWarnConfig.getDisable() && durationFilter(oomWarnConfig.getDuration())) {
            this.detectors.add(new OOMWarnDetector(this.param));
        }
    }

    private void registerSpeculativeTaskDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new SpeculativeTaskDetector(this.param));
            return;
        }
        SpeculativeTaskConfig speculativeTaskConfig = this.config.getSpeculativeTaskConfig();
        if (!speculativeTaskConfig.getDisable() && durationFilter(speculativeTaskConfig.getDuration())) {
            this.detectors.add(new SpeculativeTaskDetector(this.param));
        }
    }

    private void registerStageDurationDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new StageDurationDetector(this.param));
            return;
        }
        StageDurationConfig stageDurationConfig = this.config.getStageDurationConfig();
        if (!stageDurationConfig.getDisable()) {
            this.detectors.add(new StageDurationDetector(param));
        }
    }

    private void registerTaskDurationDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new TaskDurationDetector(this.param));
            return;
        }
        TaskDurationConfig taskDurationConfig = this.config.getTaskDurationConfig();
        if (!this.config.getTaskDurationConfig().getDisable() && durationFilter(taskDurationConfig.getDuration())) {
            this.detectors.add(new TaskDurationDetector(this.param));
        }
    }

    private void registerMRLargeTableScanDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new MRLargeTableScanDetector(this.param));
            return;
        }
        MRLargeTableScanConfig largeTableScanConfig = this.config.getMrLargeTableScanConfig();
        if (!largeTableScanConfig.getDisable() && durationFilter(largeTableScanConfig.getDuration())) {
            this.detectors.add(new MRLargeTableScanDetector(this.param));
        }
    }

    private void registerMRMemoryWasteDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new MRMemoryWasteDetector(this.param));
            return;
        }
        MRMemWasteConfig memWasteConfig = this.config.getMrMemWasteConfig();
        if (!memWasteConfig.getDisable() && durationFilter(memWasteConfig.getDuration())) {
            this.detectors.add(new MRMemoryWasteDetector(this.param));
        }
    }

    private void registerMRDataSkewDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new MRDataSkewDetector(this.param));
            return;
        }
        MRDataSkewConfig dataSkewConfig = this.config.getMrDataSkewConfig();
        if (!dataSkewConfig.getDisable() && durationFilter(dataSkewConfig.getDuration())) {
            this.detectors.add(new MRDataSkewDetector(this.param));
        }
    }

    private void registerMRSpeculativeTaskDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new MRSpeculativeTaskDetector(this.param));
            return;
        }
        MRSpeculativeTaskConfig speculativeTaskConfig = this.config.getMrSpeculativeTaskConfig();
        if (!speculativeTaskConfig.getDisable() && durationFilter(speculativeTaskConfig.getDuration())) {
            this.detectors.add(new MRSpeculativeTaskDetector(this.param));
        }
    }

    private void registerMRTaskDurationDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new MRTaskDurationDetector(this.param));
            return;
        }
        MRTaskDurationConfig taskDurationConfig = this.config.getMrTaskDurationConfig();
        if (!taskDurationConfig.getDisable() && durationFilter(taskDurationConfig.getDuration())) {
            this.detectors.add(new MRTaskDurationDetector(this.param));
        }
    }

    private void registerMRGCDetector() {
        if (this.param.isOneClick()) {
            this.detectors.add(new MRGCDetector(this.param));
            return;
        }
        MRGCConfig gcConfig = this.config.getMrGCConfig();
        if (!gcConfig.getDisable() && durationFilter(gcConfig.getDuration())) {
            this.detectors.add(new MRGCDetector(this.param));
        }
    }

    private boolean durationFilter(Long duration) {
        return this.param.getAppDuration() >= duration;
    }
}
