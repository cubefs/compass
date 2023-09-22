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

package com.oppo.cloud.portal.dao;

import com.oppo.cloud.mapper.TaskInstanceMapper;
import com.oppo.cloud.model.Task;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskExtendMapper extends TaskInstanceMapper {

    /**
     * 任务模糊查询
     */
    List<Task> searchTasksLike(@Param("projectName") String projectName, @Param("flowName") String flowName,
                           @Param("taskName") String taskName);


}
