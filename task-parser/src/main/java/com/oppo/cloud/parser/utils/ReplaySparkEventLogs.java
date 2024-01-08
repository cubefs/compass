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

package com.oppo.cloud.parser.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.domain.spark.eventlog.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * spark event log parser
 */
@Slf4j
@Data
public class ReplaySparkEventLogs {

    private SparkApplication application;
    private Map<Integer, SparkJob> jobs;
    private Map<Integer, Long> jobSQLExecIDMap;
    private Map<String, SparkExecutor> executors;
    private Map<Long, SparkTask> tasks;
    private List<SparkBlockManager> blockManagers;
    private List<StageInfo> failedStages;
    private List<SparkListenerDriverAccumUpdates> driverAccumUpdates;
    private List<SparkListenerSQLExecutionStart> sqlExecutionStarts;
    private Map<Long, AccumulableInfo> accumulableInfoMap;
    private Map<Long, Long> driverUpdateMap;
    private List<String> rawSQLExecutions;
    private ObjectMapper objectMapper;
    private Map<Integer, Integer> stageIDToJobID;
    private long logSize;

    public ReplaySparkEventLogs() {
        application = new SparkApplication();
        jobs = new HashMap<>();
        jobSQLExecIDMap = new HashMap<>();
        executors = new HashMap<>();
        tasks = new HashMap<>();
        blockManagers = new ArrayList<>();
        failedStages = new ArrayList<>();
        driverAccumUpdates = new ArrayList<>();
        sqlExecutionStarts = new ArrayList<>();
        accumulableInfoMap = new HashMap<>();
        driverUpdateMap = new HashMap<>();
        rawSQLExecutions = new ArrayList<>();
        stageIDToJobID = new HashMap<>();
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void replay(ReaderObject readerObject) throws Exception {
        String compressCodec = getCompressCodec(readerObject.getLogPath());
        while (true) {
            String line;
            try {
                line = readerObject.getBufferedReader(compressCodec).readLine();
            } catch (IOException e) {
                log.error(e.getMessage());
                break;
            }
            if (line == null) {
                break;
            }
            parseLine(line);
        }
        this.correlate();
    }

    public void replay(String[] lines) throws Exception {
        for (String line : lines) {
            parseLine(line);
        }
        this.correlate();
    }

    /**
     * Parse line by line
     */
    private void parseLine(String line) throws Exception {
        SparkListenerEvent event;
        try {
            event = objectMapper.readValue(line, SparkListenerEvent.class);
        } catch (JsonProcessingException e) {
            log.error("parseSparkEventErr:{}", line);
            return;
        }
        switch (event.getEvent()) {
            case "SparkListenerApplicationStart":
                SparkListenerApplicationStart sparkListenerApplicationStart = objectMapper.readValue(line,
                        SparkListenerApplicationStart.class);
                this.application.setAppStartTimestamp(sparkListenerApplicationStart.getTime());
                break;
            case "SparkListenerApplicationEnd":
                SparkListenerApplicationEnd sparkListenerApplicationEnd = objectMapper.readValue(line,
                        SparkListenerApplicationEnd.class);
                this.application.setAppEndTimestamp(sparkListenerApplicationEnd.getTime());
                break;
            case "SparkListenerBlockManagerAdded":
                SparkListenerBlockManagerAdded sparkListenerBlockManagerAdded = objectMapper.readValue(line,
                        SparkListenerBlockManagerAdded.class);
                SparkBlockManager blockManager = new SparkBlockManager(sparkListenerBlockManagerAdded);
                this.blockManagers.add(blockManager);
                break;
            case "SparkListenerBlockManagerRemoved":
                SparkListenerBlockManagerRemoved sparkListenerBlockManagerRemoved = objectMapper.readValue(line,
                        SparkListenerBlockManagerRemoved.class);
                break;
            case "org.apache.spark.sql.execution.ui.SparkListenerDriverAccumUpdates":
                SparkListenerDriverAccumUpdates sparkListenerDriverAccumUpdates = objectMapper.readValue(line,
                        SparkListenerDriverAccumUpdates.class);
                this.driverAccumUpdates.add(sparkListenerDriverAccumUpdates);
                break;
            case "SparkListenerEnvironmentUpdate":
                SparkListenerEnvironmentUpdate sparkListenerEnvironmentUpdate = objectMapper.readValue(line,
                        SparkListenerEnvironmentUpdate.class);
                this.application.setSparkApplication(sparkListenerEnvironmentUpdate);
                break;
            case "SparkListenerExecutorAdded":
                SparkListenerExecutorAdded sparkListenerExecutorAdded = objectMapper.readValue(line,
                        SparkListenerExecutorAdded.class);
                String id = sparkListenerExecutorAdded.getExecutorId();
                this.executors.put(id, new SparkExecutor(sparkListenerExecutorAdded));
                break;
            case "SparkListenerExecutorRemoved":
                SparkListenerExecutorRemoved sparkListenerExecutorRemoved = objectMapper.readValue(line,
                        SparkListenerExecutorRemoved.class);
                String execId = sparkListenerExecutorRemoved.getExecutorId();
                this.executors.get(execId).remove(sparkListenerExecutorRemoved);
                break;
            case "SparkListenerJobEnd":
                SparkListenerJobEnd sparkListenerJobEnd = objectMapper.readValue(line,
                        SparkListenerJobEnd.class);
                this.jobs.get(sparkListenerJobEnd.getJobId()).complete(sparkListenerJobEnd);
                break;
            case "SparkListenerJobStart":
                SparkListenerJobStart sparkListenerJobStart = objectMapper.readValue(line,
                        SparkListenerJobStart.class);
                if (this.jobs.get(sparkListenerJobStart.getJobId()) != null) {
                    log.error("ERROR: Duplicate job ID:{}", sparkListenerJobStart.getJobId());
                    break;
                }
                SparkJob job = new SparkJob(sparkListenerJobStart);
                this.jobs.put(job.getJobId(), job);
                // JobId sql.execution.id relationship mapping
                String sqlExecutionID = sparkListenerJobStart.getProperties().getProperty("spark.sql.execution" +
                        ".id");
                if (!StringUtils.isEmpty(sqlExecutionID)) {
                    jobSQLExecIDMap.put(job.getJobId(), Long.valueOf(sqlExecutionID));
                }
                // StateId jobId relationship mapping
                if (sparkListenerJobStart.getStageInfos() != null) {
                    sparkListenerJobStart.getStageInfos().forEach(stageInfo -> {
                        stageIDToJobID.put(stageInfo.getStageId(), sparkListenerJobStart.getJobId());
                    });
                }

                break;
            case "SparkListenerLogStart":
                SparkListenerLogStart sparkListenerLogStart = objectMapper.readValue(line,
                        SparkListenerLogStart.class);
                // this.application.setSparkVersion(sparkListenerLogStart.getSparkVersion());
                break;
            case "SparkListenerSQLAdaptiveExecutionUpdate":
                SparkListenerSQLAdaptiveExecutionUpdate sparkListenerSQLAdaptiveExecutionUpdate =
                        objectMapper.readValue(line, SparkListenerSQLAdaptiveExecutionUpdate.class);
                break;
            case "org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionEnd":
                SparkListenerSQLExecutionEnd sparkListenerSQLExecutionEnd = objectMapper.readValue(line,
                        SparkListenerSQLExecutionEnd.class);
                rawSQLExecutions.add(line);
                break;
            case "org.apache.spark.sql.execution.ui.SparkListenerSQLExecutionStart":
                SparkListenerSQLExecutionStart sparkListenerSQLExecutionStart = objectMapper.readValue(line,
                        SparkListenerSQLExecutionStart.class);
                sqlExecutionStarts.add(sparkListenerSQLExecutionStart);
                rawSQLExecutions.add(line);
                break;
            case "SparkListenerStageCompleted":
                SparkListenerStageCompleted sparkListenerStageCompleted = objectMapper.readValue(line,
                        SparkListenerStageCompleted.class);
                Integer stageId = sparkListenerStageCompleted.getStageInfo().getStageId();
                for (SparkJob sparkJob : this.jobs.values()) {
                    for (SparkStage stage : sparkJob.getStages()) {
                        if (stage.getStageId().equals(stageId)) {
                            stage.complete(sparkListenerStageCompleted);
                        }
                    }
                }
                StageInfo stageInfo = sparkListenerStageCompleted.getStageInfo();
                if (stageInfo != null && stageInfo.getFailureReason() != null
                        && !stageInfo.getFailureReason().isEmpty()) {
                    this.getFailedStages().add(stageInfo);
                }
                break;
            case "SparkListenerStageSubmitted":
                SparkListenerStageSubmitted sparkListenerStageSubmitted = objectMapper.readValue(line,
                        SparkListenerStageSubmitted.class);
                break;
            case "SparkListenerTaskEnd":
                SparkListenerTaskEnd sparkListenerTaskEnd = objectMapper.readValue(line,
                        SparkListenerTaskEnd.class);
                Long taskId = sparkListenerTaskEnd.getTaskInfo().getTaskId();
                this.tasks.get(taskId).finish(sparkListenerTaskEnd);
                // Update job data
                Integer jobID = stageIDToJobID.get(sparkListenerTaskEnd.getStageId());
                if (jobID != null && sparkListenerTaskEnd.getTaskMetrics() != null) {
                    SparkJob sparkJob = jobs.get(jobID);
                    if (sparkJob != null) {
                        Long sum = sparkJob.getExecutorRunTime() +
                                sparkListenerTaskEnd.getTaskMetrics().getExecutorRunTime();
                        sparkJob.setExecutorRunTime(sum);
                    }
                }
                break;
            case "SparkListenerTaskGettingResult":
                SparkListenerTaskGettingResult sparkListenerTaskGettingResult = objectMapper.readValue(line,
                        SparkListenerTaskGettingResult.class);
                break;
            case "SparkListenerTaskStart":
                SparkListenerTaskStart sparkListenerTaskStart = objectMapper.readValue(line,
                        SparkListenerTaskStart.class);
                taskId = sparkListenerTaskStart.getTaskInfo().getTaskId();
                this.tasks.put(taskId, new SparkTask(sparkListenerTaskStart));
                break;
            default:
                break;
        }

    }

    /**
     * Related data processing
     */
    private void correlate() {
        for (SparkBlockManager blockManager : this.blockManagers) {
            if (!"driver".equals(blockManager.getExecutorId())) {
                SparkExecutor executor = this.executors.get(blockManager.getExecutorId());
                if (executor != null) {
                    executor.getBlockManagers().add(blockManager);
                }
            }
        }

        for (SparkTask task : this.tasks.values()) {
            this.executors.get(task.getExecutorId()).getTasks().add(task);
            for (SparkJob job : this.jobs.values()) {
                for (SparkStage stage : job.getStages()) {
                    // Retry stageId mapping
                    for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                        Integer attemptId = entry.getKey();
                        if (stage.getStageId().equals(task.getStageId())
                                && attemptId.equals(task.getStageAttemptId())) {
                            if (stage.getTasksMap().containsKey(attemptId)) {
                                stage.getTasksMap().get(attemptId).add(task);
                            } else {
                                List<SparkTask> sparkTasks = new ArrayList<>();
                                sparkTasks.add(task);
                                stage.getTasksMap().put(attemptId, sparkTasks);
                            }
                        }
                    }
                }
            }
        }
        updateAccum();
    }

    /**
     * update AccumulableInfo driverUpdateMap
     */
    private void updateAccum() {
        for (SparkJob job : this.jobs.values()) {
            for (SparkStage stage : job.getStages()) {
                if (stage.getAccumulableInfos() != null) {
                    for (AccumulableInfo info : stage.getAccumulableInfos()) {
                        this.accumulableInfoMap.put(info.getId(), info);
                    }
                }
            }
        }
        for (SparkListenerDriverAccumUpdates updates : this.driverAccumUpdates) {
            for (List<Long> update : updates.getAccumUpdates()) {
                this.driverUpdateMap.put(update.get(0), update.get(1));
            }
        }
    }

    private String getCompressCodec(String fileName) {
        int lastSlashIndex = fileName.lastIndexOf("/");
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > lastSlashIndex) {
            return fileName.substring(lastDotIndex + 1);
        } else {
            return "none";
        }
    }
}
