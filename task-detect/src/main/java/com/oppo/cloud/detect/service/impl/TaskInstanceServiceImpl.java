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
 * 任务运行实例接口
 */
@Service
public class TaskInstanceServiceImpl implements TaskInstanceService {

    @Autowired
    private TaskInstanceExtendMapper taskInstanceExtendMapper;

    /**
     * 查询最近期任务执行情况数据
     */
    @Override
    public List<TaskStateHistory> searchTaskStateHistory(String projectName, String flowName, String taskName,
                                                         Date executionTime, Date endExecutionTime, Integer sampleNum) {
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        // 取近两个月的数据
        taskInstanceExample.createCriteria()
                .andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andExecutionTimeLessThan(executionTime)
                .andExecutionTimeGreaterThan(endExecutionTime);
        return taskInstanceExtendMapper.searchTaskState(taskInstanceExample);
    }

    /**
     * 查询任务近期执行耗时数据
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
     * 查询近一个月任务近期执行结束时间相对运行周期的差值数据
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
     * 查询具体执行周期的任务
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
