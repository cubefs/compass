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

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.CpuWasteAbnormal;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.JobDurationAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.JobDurationConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.job.StageDetectionInfo;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public class JobDurationDetector implements IDetector {

    private final DetectorParam param;

    private final JobDurationConfig config;

    JobDurationDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getJobDurationConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<JobDurationAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.JOB_DURATION.getCategory(), false);
        List<JobDurationAbnormal> jobLists = new ArrayList<>();
        for (Map.Entry<Integer, SparkJob> job : param.getReplayEventLogs().getJobs().entrySet()) {
            long jobStartTime = job.getValue().getSubmissionTime();
            long jobEndTime = job.getValue().getEndTime();
            long jobDuration = jobEndTime - jobStartTime;

            List<SparkStage> stages = job.getValue().getStages();

            List<StageDetectionInfo> lists = new ArrayList<>();
            for (SparkStage stage : stages) {

                for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                    Integer attemptId = entry.getKey();
                    List<SparkTask> tasks = stage.getTasksMap().get(attemptId);
                    if (tasks == null) {
                        continue;
                    }
                    Long firstLaunchTime = 0L;
                    for (SparkTask task : tasks) {
                        if (firstLaunchTime == 0 || task.getLaunchTime() < firstLaunchTime) {
                            firstLaunchTime = task.getLaunchTime();
                        }
                    }
                    Long submissionTime = entry.getValue();
                    Long completeTime = stage.getCompleteTimeMap().get(attemptId);
                    lists.add(new StageDetectionInfo(stage.getStageId(), attemptId, firstLaunchTime, submissionTime,
                            completeTime));
                }
            }

            if (lists.size() == 0) {
                continue;
            }
            // 按开始时间排序
            lists.sort(Comparator.comparing(StageDetectionInfo::getFirstLaunchTime));

            // stage累计时间
            long stageAcc = 0;
            StageDetectionInfo cur = lists.get(0);
            long start = cur.getFirstLaunchTime();
            for (int i = 1; i < lists.size(); i++) {
                StageDetectionInfo stageInfo = lists.get(i);
                if (cur.getCompleteTime() <= stageInfo.getFirstLaunchTime()) {
                    stageAcc += cur.getCompleteTime() - start;
                    start = stageInfo.getFirstLaunchTime();
                }
                if (stageInfo.getCompleteTime() > cur.getCompleteTime()) {
                    cur = stageInfo;
                }
            }
            stageAcc += cur.getCompleteTime() - start;
            if (stageAcc > jobDuration) {
                log.warn("stageAcc more than jobDuration:{},{},{},{}", this.param.getAppId(), job.getKey(), jobDuration,
                        stageAcc);
                stageAcc = jobDuration;
            }
            double threshold = this.config.getThreshold();
            long duration = this.config.getDuration();
            double ratio = ((double) (jobDuration - stageAcc) / jobDuration) * 100;
            if (ratio > threshold && jobDuration > duration) {
                // 耗时异常
                jobLists.add(
                        new JobDurationAbnormal(job.getKey(), jobDuration, stageAcc, ratio, threshold, duration, true));
                detectorResult.setAbnormal(true);
            } else {
                jobLists.add(new JobDurationAbnormal(job.getKey(), jobDuration, stageAcc, ratio, threshold, duration,
                        false));
            }
        }
        if (jobLists.size() == 0) {
            return null;
        }
        detectorResult.setData(jobLists);
        return detectorResult;
    }
}
