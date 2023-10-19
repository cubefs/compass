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

package com.oppo.cloud.parser.service.job.detector.spark;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.StageDurationAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.StageDurationConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.job.TaskDetectionInfo;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public class StageDurationDetector implements IDetector {

    private final DetectorParam param;

    private final StageDurationConfig config;

    public StageDurationDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getStageDurationConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<StageDurationAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.STAGE_DURATION.getCategory(), false);

        List<StageDurationAbnormal> stageList = new ArrayList<>();

        for (Map.Entry<Integer, SparkJob> jobs : this.param.getReplayEventLogs().getJobs().entrySet()) {
            List<SparkStage> stages = jobs.getValue().getStages();
            for (SparkStage stage : stages) {
                for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                    Integer attemptId = entry.getKey();
                    List<SparkTask> tasks = stage.getTasksMap().get(attemptId);
                    if (stage.getFailed().get(attemptId) != null) {
                        continue;
                    }
                    List<TaskDetectionInfo> taskDetectionInfoList = new ArrayList<>();
                    Long firstLaunchTime = 0L;
                    for (SparkTask task : tasks) {
                        if (firstLaunchTime == 0 || task.getLaunchTime() < firstLaunchTime) {
                            firstLaunchTime = task.getLaunchTime();
                        }

                        taskDetectionInfoList.add(new TaskDetectionInfo(jobs.getKey(), stage.getStageId(), attemptId,
                                task.getTaskId(), task.getLaunchTime(), task.getFinishTime()));
                    }

                    Long completeTime = stage.getCompleteTimeMap().get(attemptId);
                    // refer to https://github.com/apache/spark/pull/9051
                    long stageDuration = completeTime - firstLaunchTime;

                    taskDetectionInfoList.sort(Comparator.comparing(TaskDetectionInfo::getFirstLaunchTime));
                    // Cumulative task time
                    TaskDetectionInfo curTask = taskDetectionInfoList.get(0);
                    long start = curTask.getFirstLaunchTime();
                    long taskAcc = 0;

                    for (int i = 1; i < taskDetectionInfoList.size(); i++) {
                        TaskDetectionInfo taskInfo = taskDetectionInfoList.get(i);
                        if (curTask.getFinishTime() <= taskInfo.getFirstLaunchTime()) {
                            // finishTime is 0, speculative may have occurred
                            if (curTask.getFinishTime() != 0) {
                                taskAcc += curTask.getFinishTime() - start;
                            }
                            start = taskInfo.getFirstLaunchTime();
                        }
                        if (taskInfo.getFinishTime() > curTask.getFinishTime()) {
                            curTask = taskInfo;
                        }
                    }

                    taskAcc += curTask.getFinishTime() - start;
                    // finishTime time is 0, tasks in stage are always in running state
                    if (taskAcc <= 0) {
                        log.warn("taskAcc less than zero:{},{},{},{}", param.getAppId(),
                                stage.getStageId(), stageDuration, taskAcc);
                        taskAcc = stageDuration;
                    }
                    if (taskAcc > stageDuration) {
                        taskAcc = stageDuration;
                    }

                    double ratio = ((double) (stageDuration - taskAcc) / stageDuration) * 100;
                    double threshold = this.config.getThreshold();
                    long duration = this.config.getDuration();
                    if (ratio > threshold && stageDuration > duration) {
                        // Abnormal duration
                        stageList.add(new StageDurationAbnormal(jobs.getKey(), stage.getStageId(), attemptId,
                                stageDuration, taskAcc, ratio, threshold, duration, true));
                        detectorResult.setAbnormal(true);
                    } else {
                        stageList.add(new StageDurationAbnormal(jobs.getKey(), stage.getStageId(), attemptId,
                                stageDuration, taskAcc, ratio, threshold, duration, false));
                    }

                }

            }
        }
        if (stageList.size() == 0) {
            return null;
        }
        detectorResult.setData(stageList);
        return detectorResult;
    }
}
