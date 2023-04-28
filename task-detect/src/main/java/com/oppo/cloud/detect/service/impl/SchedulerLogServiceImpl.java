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

import com.oppo.cloud.common.util.ui.TryNumberUtil;
import com.oppo.cloud.detect.service.SchedulerLogService;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.model.TaskApplication;
import com.oppo.cloud.model.TaskApplicationExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 调度日志接口实现类
 */
@Slf4j
@Service
public class SchedulerLogServiceImpl implements SchedulerLogService {

    @Value("${custom.schedulerType}")
    private String schedulerType;

    @Autowired
    private TaskApplicationMapper taskApplicationMapper;


    @Override
    public List<String> getSchedulerLog(String projectName, String flowName, String taskName, Date executionDate,
                                        Integer tryNum) {
        TaskApplicationExample taskApplicationExample = new TaskApplicationExample();
        taskApplicationExample.createCriteria()
                .andProjectNameEqualTo(projectName)
                .andFlowNameEqualTo(flowName)
                .andTaskNameEqualTo(taskName)
                .andExecuteTimeEqualTo(executionDate);
        List<TaskApplication> taskApplicationList =
                taskApplicationMapper.selectByExampleWithBLOBs(taskApplicationExample);
        if (taskApplicationList.size() != 0) {
            TaskApplication taskApplication = null;
            for (TaskApplication temp : taskApplicationList) {
                temp.setRetryTimes(TryNumberUtil.updateTryNumber(temp.getRetryTimes(), schedulerType));
                if (temp.getRetryTimes().equals(tryNum)) {
                    taskApplication = temp;
                    break;
                }
            }
            // 兼容无调度周期的任务重试次数默认为0
            if (taskApplication == null) {
                taskApplication = taskApplicationList.get(0);
            }
            if (!StringUtils.isEmpty(taskApplication.getLogPath())) {
                return Arrays.asList(taskApplication.getLogPath().split(","));
            }
        }
        log.error(
                "can not find scheduler log from task_application,taskName:{},flowName:{}, executionDate:{}, tryNum:{}",
                taskName, flowName, executionDate, tryNum);
        return null;
    }
}
