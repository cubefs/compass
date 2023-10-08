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

import com.oppo.cloud.model.TaskInstance;
import com.oppo.cloud.syncer.util.DataUtil;

import java.util.Map;

/**
 * Task running instance build
 */
public class TaskInstanceBuilder implements DataBuilder<TaskInstance> {

    /**
     * Task scheduler triggers
     */
    private static final String TRIGGER_TYPE_SCHEDULE = "schedule";
    /**
     * Tasks are manually clicked to trigger
     */
    private static final String TRIGGER_TYPE_MANUAL = "manual";

    @Override
    public TaskInstance run(Map<String, String> data) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(DataUtil.parseInteger(data.get("id")));
        taskInstance.setProjectName(data.getOrDefault("project_name", ""));
        taskInstance.setFlowName(data.get("flow_name"));
        taskInstance.setTaskName(data.get("task_name"));

        taskInstance.setStartTime(DataUtil.parseDate(data.get("start_time")));
        taskInstance.setEndTime(DataUtil.parseDate(data.get("end_time")));
        taskInstance.setExecutionTime(DataUtil.parseDate(data.get("execution_time")));

        taskInstance.setTaskState(data.get("task_state"));
        taskInstance.setTaskType(data.get("task_type"));
        taskInstance.setRetryTimes(DataUtil.parseInteger(data.get("retry_times")));
        taskInstance.setMaxRetryTimes(DataUtil.parseInteger(data.get("max_retry_times")));
        taskInstance.setWorkerGroup(data.get("worker_group"));
        taskInstance.setCreateTime(DataUtil.parseDate(data.get("create_time")));
        taskInstance.setUpdateTime(DataUtil.parseDate(data.get("update_time")));

        // If the periodic execution cycle time is not available, the start time is used
        if (taskInstance.getExecutionTime() == null) {
            taskInstance.setExecutionTime(taskInstance.getStartTime());
            taskInstance.setTriggerType(TRIGGER_TYPE_MANUAL);
        } else {
            taskInstance.setTriggerType(TRIGGER_TYPE_SCHEDULE);
        }
        return taskInstance;
    }
}
