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

import com.oppo.cloud.detect.mapper.TaskInstanceExtendMapper;
import com.oppo.cloud.detect.service.SchedulerLogService;
import com.oppo.cloud.model.TaskInstance;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;


@SpringBootTest
class SchedulerLogServiceImplTest {

    @MockBean(name = "taskInstanceExtendMapper")
    TaskInstanceExtendMapper taskInstanceExtendMapper;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private SchedulerLogService schedulerLogService;

    @Test
    void getDolphinLog() {
        List<TaskInstance> taskInstances = new ArrayList<>();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(10);
        taskInstances.add(taskInstance);
        Mockito.when(taskInstanceExtendMapper.selectByExample(Mockito.any())).thenReturn(taskInstances);
        Map<String, Object> depData = new HashMap<>();
        depData.put("end_time", new Date());
        depData.put("log_path", "/home/service/logs/2045/2013.log");
        Mockito.when(jdbcTemplate.queryForMap(Mockito.anyString())).thenReturn(depData);
    }

}
