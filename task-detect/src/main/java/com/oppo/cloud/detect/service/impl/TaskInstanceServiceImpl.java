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

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oppo.cloud.common.constant.TaskStateEnum;
import com.oppo.cloud.detect.domain.TaskStateHistory;
import com.oppo.cloud.detect.mapper.TaskInstanceExtendMapper;
import com.oppo.cloud.detect.service.TaskInstanceService;
import com.oppo.cloud.model.TaskInstance;
import com.oppo.cloud.model.TaskInstanceExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Task execution instance interface.
 */
@Service
public class TaskInstanceServiceImpl implements TaskInstanceService {

    @Autowired
    private TaskInstanceExtendMapper taskInstanceExtendMapper;

    /**
     * Query the data of the most recent task execution.
     */
    @Override
    public List<TaskStateHistory> searchTaskStateHistory(String projectName, String flowName, String taskName,
                                                         Date executionTime, Date endExecutionTime, Integer sampleNum) {
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        // Take data from the past two months.
        taskInstanceExample.createCriteria()
                .andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andExecutionTimeLessThan(executionTime)
                .andExecutionTimeGreaterThan(endExecutionTime);
        return taskInstanceExtendMapper.searchTaskState(taskInstanceExample);
    }

    /**
     * Query the recent execution duration data of tasks.
     */
    @Override
    public List<Double> searchTaskDurationHistory(String projectName, String flowName, String taskName,
                                                  Date executionTime, Date endExecutionTime, Integer sampleNum) {
        List<Double> result = new ArrayList<>();
        PageHelper.startPage(1, sampleNum, false);
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        taskInstanceExample.createCriteria()
                .andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andTaskStateEqualTo(TaskStateEnum.success.name())
                .andExecutionTimeLessThan(executionTime)
                .andExecutionTimeGreaterThan(endExecutionTime);
        List<TaskStateHistory> taskStateHistoryList = taskInstanceExtendMapper.searchTaskDuration(taskInstanceExample);
        for (TaskStateHistory taskStateHistory : taskStateHistoryList) {
            if (TaskStateEnum.success.name().equals(taskStateHistory.getState())) {
                result.add(taskStateHistory.getValue());
            }
        }
        return result;
    }

    /**
     * Query the data of the difference between the end time of task execution and the running cycle in the past month.
     */
    @Override
    public List<Double> searchTaskRelativeEndTime(String projectName, String flowName, String taskName,
                                                  Date executionTime, Date endExecutionTime, Integer sampleNum) {
        List<Double> result = new ArrayList<>();
        PageHelper.startPage(1, sampleNum, false);
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        taskInstanceExample.createCriteria()
                .andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andTaskStateEqualTo(TaskStateEnum.success.name())
                .andExecutionTimeLessThan(executionTime)
                .andExecutionTimeGreaterThan(endExecutionTime);
        List<TaskStateHistory> taskStateHistoryList =
                taskInstanceExtendMapper.searchTaskRelativeEndTime(taskInstanceExample);
        for (TaskStateHistory taskStateHistory : taskStateHistoryList) {
            if (taskStateHistory.getValue() != null) {
                result.add(taskStateHistory.getValue());
            }
        }
        return result;
    }

    /**
     * Query specific execution cycles of tasks.
     */
    @Override
    public TaskInstance searchTaskSum(String projectName, String flowName, String taskName, Date executionTime) {
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        taskInstanceExample.setOrderByClause("retry_times asc");
        taskInstanceExample.createCriteria().andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andExecutionTimeEqualTo(executionTime);
        List<TaskInstance> taskInstances = taskInstanceExtendMapper.selectByExample(taskInstanceExample);
        if (taskInstances.size() != 0) {
            Date startTime = taskInstances.get(0).getStartTime();
            TaskInstance taskInstance = taskInstances.get(taskInstances.size() - 1);
            taskInstance.setStartTime(startTime);
            return taskInstance;
        } else {
            return null;
        }
    }
}
