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

package com.oppo.cloud.detect.mapper;

import com.oppo.cloud.detect.domain.TaskStateHistory;
import com.oppo.cloud.mapper.TaskInstanceMapper;
import com.oppo.cloud.model.TaskInstanceExample;

import java.util.List;

public interface TaskInstanceExtendMapper extends TaskInstanceMapper {

    /**
     * Query the historical data of task execution status.
     */
    List<TaskStateHistory> searchTaskState(TaskInstanceExample example);

    /**
     * Query the historical data of task execution duration.
     */
    List<TaskStateHistory> searchTaskDuration(TaskInstanceExample example);

    /**
     * Query the historical data of the end time of task execution relative to the running cycle.
     */
    List<TaskStateHistory> searchTaskRelativeEndTime(TaskInstanceExample example);
}
