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

@Data
@NoArgsConstructor
public class SparkTask {

    private Long taskId;
    private Integer stageId;
    private Integer stageAttemptId;
    private String executorId;
    private Long launchTime;
    private String taskLocality;
    private Boolean speculative;

    private String endReason = "";
    public String failedMessage = "";
    public String fullStackTrace = "";
    public String lossReason = "";
    private Boolean failed = false;
    private Long finishTime = 0L;
    private Long gettingResultTime = 0L;
    private Integer index = 0;
    private String taskType = "";
    private Boolean hasMetric = false;
    private Long diskBytesSpilled = 0L;
    private Long memoryBytesSpilled = 0L;
    private Long executorDeserializeTime = 0L;
    private Long executorRunTime = 0L;
    private Long jvmGCTime = 0L;
    private Long resultSerializationTime = 0L;
    private Long resultSize = 0L;
    /**
     * Total Shuffle Read  =  Remote Bytes Read + Local Bytes Read
     */
    private Long totalShuffleReadBytes = 0L;
    private Long totalRecordsRead = 0L;
    /**
     * task InputMetrics
     */
    private Long bytesRead = 0L;

    public SparkTask(SparkListenerTaskStart start) {
        this.taskId = start.getTaskInfo().getTaskId();
        this.stageId = start.getStageId();
        this.stageAttemptId = start.getStageAttemptId();
        this.executorId = start.getTaskInfo().getExecutorId();
        this.launchTime = start.getTaskInfo().getLaunchTime();
        this.taskLocality = start.getTaskInfo().getTaskLocality();
        this.speculative = start.getTaskInfo().getSpeculative();
    }

    public void finish(SparkListenerTaskEnd end) {
        if (end.getReason() != null) {
            this.endReason = end.getReason().getReason();
            this.failedMessage = end.getReason().getMessage();
            this.fullStackTrace = end.getReason().getFullStackTrace();
            this.lossReason = end.getReason().getLossReason();
        }
        if (end.getTaskInfo() != null) {
            this.failed = end.getTaskInfo().getFailed();
            this.finishTime = end.getTaskInfo().getFinishTime();
            this.gettingResultTime = end.getTaskInfo().getGettingResultTime();
            this.index = end.getTaskInfo().getIndex();
        }
        this.taskType = end.getTaskType();
        if (end.getTaskMetrics() != null) {
            this.hasMetric = true;
            this.diskBytesSpilled = end.getTaskMetrics().getDiskBytesSpilled();
            this.memoryBytesSpilled = end.getTaskMetrics().getMemoryBytesSpilled();
            this.executorDeserializeTime = end.getTaskMetrics().getExecutorDeserializeTime();
            this.executorRunTime = end.getTaskMetrics().getExecutorRunTime();
            this.jvmGCTime = end.getTaskMetrics().getJvmGCTime();
            this.resultSerializationTime = end.getTaskMetrics().getResultSerializationTime();
            this.resultSize = end.getTaskMetrics().getResultSize();
            if (end.getTaskMetrics().getShuffleReadMetrics() != null) {
                totalShuffleReadBytes = end.getTaskMetrics().getShuffleReadMetrics().getRemoteBytesRead() +
                        end.getTaskMetrics().getShuffleReadMetrics().getLocalBytesRead();
                totalRecordsRead = end.getTaskMetrics().getShuffleReadMetrics().getRecordsRead();
            }
            if (end.getTaskMetrics().getInputMetrics() != null) {
                this.bytesRead = end.getTaskMetrics().getInputMetrics().getBytesRead();
            }

        }
    }

}
