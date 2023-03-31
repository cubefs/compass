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

package com.oppo.cloud.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.domain.job.Datum;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.mapper.TaskDatumMapper;
import com.oppo.cloud.model.TaskDatum;
import com.oppo.cloud.model.TaskDatumExample;
import com.oppo.cloud.model.TaskInstance;
import com.oppo.cloud.model.TaskInstanceExample;
import com.oppo.cloud.portal.dao.TaskInstanceExtendMapper;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.ValueInfo;
import com.oppo.cloud.portal.domain.task.JobDetailRequest;
import com.oppo.cloud.portal.service.TaskInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class TaskInstanceServiceImpl implements TaskInstanceService {

    @Autowired
    private TaskInstanceExtendMapper taskInstanceMapper;

    @Autowired
    private TaskDatumMapper taskDatumMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<MetricInfo> getJobDurationTrend(JobDetailRequest jobDetailRequest) throws Exception {
        List<MetricInfo> metricInfoList = new ArrayList<>();
        PageHelper.startPage(1, 20, false);
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        taskInstanceExample.createCriteria().andTaskNameEqualTo(jobDetailRequest.getTaskName())
                .andFlowNameEqualTo(jobDetailRequest.getFlowName())
                .andExecutionTimeLessThan(jobDetailRequest.getExecutionDate())
                .andProjectNameEqualTo(jobDetailRequest.getProjectName());
        taskInstanceExample.setOrderByClause("execution_time asc");
        List<TaskInstance> taskInstanceList = taskInstanceMapper.selectByExample(taskInstanceExample);
        for (TaskInstance taskInstance : taskInstanceList) {
            long duration = (taskInstance.getEndTime().getTime() - taskInstance.getStartTime().getTime()) / 1000;
            MetricInfo metricInfo = new MetricInfo();
            metricInfo.setXValue(DateUtil.format(taskInstance.getExecutionTime()));
            metricInfo.setYValues(Collections.singletonList(new ValueInfo(duration, "duration")));
            metricInfoList.add(metricInfo);
        }
        return metricInfoList;
    }

    @Override
    public Datum getJobDatum(JobDetailRequest jobDetailRequest) throws Exception {
        TaskDatumExample jobDatumExample = new TaskDatumExample();
        jobDatumExample.createCriteria().andProjectNameEqualTo(jobDetailRequest.getProjectName())
                .andFlowNameEqualTo(jobDetailRequest.getFlowName())
                .andTaskNameEqualTo(jobDetailRequest.getTaskName())
                .andExecutionDateEqualTo(jobDetailRequest.getExecutionDate());
        List<TaskDatum> jobDatumList = taskDatumMapper.selectByExampleWithBLOBs(jobDatumExample);
        if (jobDatumList.size() > 0) {
            String baseline = jobDatumList.get(0).getBaseline();
            return objectMapper.readValue(baseline, Datum.class);
        }
        return null;
    }

    @Override
    public TaskInstance searchTaskSum(String projectName, String flowName, String taskName, Date executionTime) {
        TaskInstanceExample taskInstanceExample = new TaskInstanceExample();
        taskInstanceExample.setOrderByClause("retry_times asc");
        taskInstanceExample.createCriteria().andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andExecutionTimeEqualTo(executionTime);
        List<TaskInstance> taskInstances = taskInstanceMapper.selectByExample(taskInstanceExample);
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
