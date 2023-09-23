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

import java.util.ArrayList;
import java.util.List;

@Data
public class SparkJob {

    private Integer jobId;
    private List<SparkStage> stages;
    private Long submissionTime;
    private Long endTime;
    private JobResult jobResult;
    /**
     * executor 运行时间
     */
    private Long executorRunTime = 0L;

    public SparkJob() {
        this.stages = new ArrayList<>();
    }

    public SparkJob(SparkListenerJobStart jobStart) {
        this.stages = new ArrayList<>();
        this.jobId = jobStart.getJobId();
        for (StageInfo stageInfo : jobStart.getStageInfos()) {
            this.stages.add(new SparkStage(stageInfo));
        }
        this.submissionTime = jobStart.getTime();
    }

    public void complete(SparkListenerJobEnd jobEnd) {
        this.jobResult = jobEnd.getJobResult();
        this.endTime = jobEnd.getTime();
    }

}
