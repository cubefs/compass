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

package com.oppo.cloud.parser.domain.spark.eventlog;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@NoArgsConstructor
public class SparkStage {

    private Integer stageId;
    private String details;
    private Integer numTasks;
    private List<SparkRDD> RDDs;
    private String name;
    private Map<Integer, List<SparkTask>> tasksMap;
    /**
     * Stage with retry
     */
    private Map<Integer, Boolean> failed;
    private Map<Integer, Long> completeTimeMap;
    private Map<Integer, Long> submissionTimeMap;
    private List<AccumulableInfo> accumulableInfos;

    public SparkStage(StageInfo stageInfo) {
        this.stageId = stageInfo.getStageId();
        this.details = stageInfo.getDetails();
        this.numTasks = stageInfo.getNumTasks();
        this.RDDs = new ArrayList<>();
        this.tasksMap = new HashMap<>();
        this.failed = new HashMap<>();
        this.completeTimeMap = new HashMap<>();
        this.submissionTimeMap = new HashMap<>();

        for (RDDInfo rddInfo : stageInfo.getRddInfos()) {
            this.RDDs.add(new SparkRDD(rddInfo));
        }
        this.name = stageInfo.getName();
    }

    public void complete(SparkListenerStageCompleted completed) {
        this.accumulableInfos = completed.getStageInfo().getAccumulables();

        this.completeTimeMap.put(completed.getStageInfo().getAttemptNumber(),
                completed.getStageInfo().getCompleteTime());
        this.submissionTimeMap.put(completed.getStageInfo().getAttemptNumber(),
                completed.getStageInfo().getSubmissionTime());
        if (!StringUtils.isEmpty(completed.getStageInfo().getFailureReason())) {
            failed.put(completed.getStageInfo().getAttemptNumber(), true);
        }
    }
}
