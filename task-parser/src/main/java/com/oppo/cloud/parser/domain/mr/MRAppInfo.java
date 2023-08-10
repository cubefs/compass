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

package com.oppo.cloud.parser.domain.mr;

import com.oppo.cloud.parser.domain.mr.event.TaskInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MRAppInfo {
    private String jobId;
    private String errorInfo;
    private String username;
    private String jobName;
    private String jobQueueName;
    private Long submitTime;
    private Long launchTime;
    private Integer totalMaps;
    private Integer totalReduces;
    private Integer failedMaps;
    private Integer failedReduces;
    private Integer succeededMaps;
    private Integer succeededReduces;
    private Integer killedMaps;
    private Integer killedReduces;
    private Long finishTime;
    private Long elapsedTime;
    private String jobStatus;
    private Map<String, String> confMap;
    private Map<String, TaskInfo> tasksMap;
    private Map<String, Map<String, Long>> totalCounters;
    private List<MRTaskAttemptInfo> mapList;
    private List<MRTaskAttemptInfo> reduceList;
    private SpeculationInfo speculationInfo;

    public MRAppInfo() {
        this.tasksMap = new HashMap<>();
        this.totalCounters = new HashMap<>();
        this.mapList = new ArrayList<>();
        this.reduceList = new ArrayList<>();
    }

}
