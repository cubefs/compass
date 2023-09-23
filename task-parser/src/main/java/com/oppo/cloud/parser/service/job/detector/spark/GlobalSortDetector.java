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
import com.oppo.cloud.common.domain.eventlog.GlobalSortAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.GlobalSortConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class GlobalSortDetector implements IDetector {

    private final DetectorParam param;

    private final GlobalSortConfig config;

    public GlobalSortDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getGlobalSortConfig();

    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<GlobalSortAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.GLOBAL_SORT.getCategory(), false);

        List<GlobalSortAbnormal> globalSortAbnormalList = new ArrayList<>();
        for (Map.Entry<Integer, SparkJob> job : this.param.getReplayEventLogs().getJobs().entrySet()) {
            List<SparkStage> stages = job.getValue().getStages();
            for (SparkStage stage : stages) {
                for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                    Integer attemptId = entry.getKey();
                    List<SparkTask> taskList = stage.getTasksMap().get(attemptId);
                    if (taskList == null) {
                        continue;
                    }
                    if (taskList.size() != this.param.getConfig().getGlobalSortConfig().getTaskCount()) {
                        continue;
                    }
                    SparkTask sparkTask = taskList.get(0);
                    long duration = sparkTask.getFinishTime() - sparkTask.getLaunchTime();

                    if (sparkTask.getTotalRecordsRead() > this.config.getRecords()
                            && duration > this.config.getDuration()) {
                        detectorResult.setAbnormal(true);
                        globalSortAbnormalList.add(new GlobalSortAbnormal(
                                job.getKey(), stage.getStageId(), attemptId, sparkTask.getTaskId(),
                                sparkTask.getTotalRecordsRead(), duration, true));
                    }
                }
            }
        }

        if (detectorResult.getAbnormal()) {
            detectorResult.setData(globalSortAbnormalList);
            return detectorResult;
        }

        return null;
    }
}
