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
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.SpeculativeTaskAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.SpeculativeTaskConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpeculativeTaskDetector implements IDetector {

    private DetectorParam param;

    private SpeculativeTaskConfig config;

    SpeculativeTaskDetector(DetectorParam detectorParam) {
        this.param = detectorParam;
        this.config = detectorParam.getConfig().getSpeculativeTaskConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<SpeculativeTaskAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.SPECULATIVE_TASK.getCategory(), false);

        List<SpeculativeTaskAbnormal> speculativeTaskAbnormalList = new ArrayList<>();

        for (Map.Entry<Integer, SparkJob> job : this.param.getReplayEventLogs().getJobs().entrySet()) {
            List<SparkStage> stages = job.getValue().getStages();
            for (SparkStage stage : stages) {
                for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                    Integer attemptId = entry.getKey();
                    List<SparkTask> tasks = stage.getTasksMap().get(attemptId);
                    if (tasks == null) {
                        continue;
                    }
                    List<Long> taskIds = new ArrayList<>();
                    for (SparkTask task : tasks) {
                        // 推测执行
                        if (task.getSpeculative() != null && task.getSpeculative()) {
                            taskIds.add(task.getTaskId());
                        }
                    }
                    if (taskIds.size() == 0) {
                        continue;
                    }

                    long threshold = this.config.getThreshold();
                    if (taskIds.size() > threshold && this.param.getAppDuration() > this.config.getDuration()) {
                        detectorResult.setAbnormal(true);
                        speculativeTaskAbnormalList.add(new SpeculativeTaskAbnormal(job.getKey(), stage.getStageId(),
                                attemptId, taskIds.size(), taskIds, true, threshold));
                    } else {
                        speculativeTaskAbnormalList.add(new SpeculativeTaskAbnormal(job.getKey(), stage.getStageId(),
                                attemptId, taskIds.size(), taskIds, false, threshold));
                    }
                }
            }
        }

        if (speculativeTaskAbnormalList.size() == 0) {
            return null;
        }

        detectorResult.setData(speculativeTaskAbnormalList);
        return detectorResult;
    }
}
