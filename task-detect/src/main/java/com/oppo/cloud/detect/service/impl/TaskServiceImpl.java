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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.detect.service.TaskService;
import com.oppo.cloud.mapper.TaskMapper;
import com.oppo.cloud.model.Task;
import com.oppo.cloud.model.TaskExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public Task getTask(String projectName, String flowName, String taskName) {
        TaskExample taskExample = new TaskExample();
        taskExample.createCriteria().andTaskNameEqualTo(taskName)
                .andFlowNameEqualTo(flowName)
                .andProjectNameEqualTo(projectName);
        List<Task> tasks = taskMapper.selectByExample(taskExample);
        if (tasks.size() > 0) {
            return tasks.get(0);
        }
        return null;
    }

    @Override
    public Task getTask(Integer id) {
        TaskExample taskExample = new TaskExample();
        taskExample.createCriteria().andIdEqualTo(id);
        List<Task> tasks = taskMapper.selectByExample(taskExample);
        if (tasks.size() > 0) {
            return tasks.get(0);
        }
        return null;
    }
}
