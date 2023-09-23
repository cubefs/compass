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

package com.oppo.cloud.common.domain.eventlog;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DetectionStorage {

    private String applicationId;
    private String dagId;
    private String oflowTaskId;
    private Integer executionDate;
    private Integer retryNumber;
    private String appCategory;
    private String action;
    private String desc;
    private String rawLog;
    private String zone;
    private String logPath;
    private Map<String, String> vars;
    /**
     * 是否异常
     */
    private Boolean abnormal;
    /**
     * 数据倾斜
     */
    private List<DataSkewAbnormal> dataSkewTasks;
    /**
     * 大表扫描
     */
    private LargeTableScanAbnormal largeTableScanAbnormal;
    /**
     * oom预警
     */
    private OOMAbnormal oomAbnormal;
    /**
     * job异常数据
     */
    private List<JobDurationAbnormal> jobDurationAbnormal;
    /**
     * stage异常数据
     */
    private List<StageDurationAbnormal> stageDurationAbnormal;
    /**
     * task异常数据
     */
    private List<TaskDurationAbnormal> taskDurationAbnormal;
    /**
     * hdfs卡顿
     */
    private List<HdfsStuckAbnormal> hdfsStuckAbnormal;
    /**
     * 推测执行过多异常
     */
    private List<SpeculativeTaskAbnormal> speculativeTaskAbnormal;
    /**
     * 全局排序异常
     */
    private List<GlobalSortAbnormal> globalSortAbnormal;
    /**
     * cpu浪费
     */
    private CpuWasteAbnormal cpuWasteAbnormal;
    /**
     * 内存浪费
     */
    private MemWasteAbnormal memWasteAbnormal;

    public DetectionStorage() {

    }

    public DetectionStorage(String applicationId, String dagId, String oflowTaskId, Integer executionDate,
                            Integer retryNumber, String appCategory, String action, String desc, String rawLog,
                            String zone, String logPath, boolean abnormal) {
        this.applicationId = applicationId;
        this.dagId = dagId;
        this.oflowTaskId = oflowTaskId;
        this.executionDate = executionDate;
        this.retryNumber = retryNumber;
        this.appCategory = appCategory;
        this.action = action;
        this.desc = desc;
        this.action = action;
        this.rawLog = rawLog;
        this.zone = zone;
        this.logPath = logPath;
        this.abnormal = abnormal;

    }
}
