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

import com.oppo.cloud.detect.service.SchedulerLogService;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.model.TaskApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;


@SpringBootTest(classes = SchedulerLogServiceImpl.class)
class SchedulerLogServiceImplTest {

    @MockBean(name = "taskApplicationMapper")
    TaskApplicationMapper taskApplicationMapper;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private SchedulerLogService schedulerLogService;

    @Test
    void getSchedulerLog() {
        List<TaskApplication> taskApplications = new ArrayList<>();
        TaskApplication taskApp = new TaskApplication();
        taskApp.setId(10);
        taskApp.setRetryTimes(1);
        taskApp.setLogPath("/home/service/logs/2045/2013.log");
        taskApplications.add(taskApp);
        Mockito.when(taskApplicationMapper.selectByExampleWithBLOBs(Mockito.any())).thenReturn(taskApplications);

        List<String> schedulerLog = schedulerLogService.getSchedulerLog("project", "flow", "task", new Date(), 1);
        Assertions.assertTrue(schedulerLog.size() == 1);
    }

}
