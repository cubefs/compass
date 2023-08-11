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

package com.oppo.cloud.syncer.util.databuild;

import com.oppo.cloud.model.Task;
import com.oppo.cloud.syncer.util.DataUtil;

import java.util.Map;

/**
 * 定义任务数据构建
 */
public class TaskBuilder implements DataBuilder<Task> {

    @Override
    public Task run(Map<String, String> data) {
        Task task = new Task();
        task.setId(DataUtil.parseInteger(data.get("id")));
        task.setProjectName(data.getOrDefault("project_name", ""));
        task.setProjectId(DataUtil.parseInteger(data.get("project_id")));
        task.setFlowName(data.get("flow_name"));
        task.setFlowId(DataUtil.parseInteger(data.get("flow_id")));
        task.setTaskName(data.get("task_name"));
        task.setDescription(data.get("description"));
        task.setUserId(DataUtil.parseInteger(data.get("user_id")));
        task.setTaskType(data.get("task_type"));
        task.setRetries(DataUtil.parseInteger(data.get("retries")));
        task.setCreateTime(DataUtil.parseDate(data.get("create_time")));
        task.setUpdateTime(DataUtil.parseDate(data.get("update_time")));
        return task;
    }
}
