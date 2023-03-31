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
     *查询任务执行状态的历史数据
     */
    List<TaskStateHistory> searchTaskState(TaskInstanceExample example);

    /**
     * 查询任务执行耗时的历史数据
     */
    List<TaskStateHistory> searchTaskDuration(TaskInstanceExample example);

    /**
     * 查询任务执行结束时间相对于运行周期的历史数据
     */
    List<TaskStateHistory> searchTaskRelativeEndTime(TaskInstanceExample example);
}
