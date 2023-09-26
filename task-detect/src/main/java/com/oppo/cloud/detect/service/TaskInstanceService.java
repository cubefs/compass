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

package com.oppo.cloud.detect.service;

import com.oppo.cloud.detect.domain.TaskStateHistory;
import com.oppo.cloud.model.TaskInstance;

import java.util.Date;
import java.util.List;

/**
 * Task execution instance interface.
 */
public interface TaskInstanceService {

    /**
     * Query the data of recent task execution.
     */
    List<TaskStateHistory> searchTaskStateHistory(String projectName, String flowName, String taskName,
                                                  Date executionTime, Date endExecutionTime, Integer sampleNum);

    /**
     * Query the recent execution duration data of tasks.
     */
    List<Double> searchTaskDurationHistory(String projectName, String flowName, String taskName,
                                           Date executionTime, Date endExecutionTime, Integer sampleNum);

    /**
     * Query the data of the difference between the end time of task execution and the running cycle in recent times.
     */
    List<Double> searchTaskRelativeEndTime(String projectName, String flowName, String taskName,
                                           Date executionTime, Date endExecutionTime, Integer sampleNum);

    /**
     * Query aggregated tasks for a specific execution cycle.
     */
    TaskInstance searchTaskSum(String projectName, String flowName, String taskName,
                               Date executionTime);
}
