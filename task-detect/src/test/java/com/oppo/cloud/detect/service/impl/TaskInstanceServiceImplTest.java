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

import com.oppo.cloud.detect.domain.TaskStateHistory;
import com.oppo.cloud.detect.mapper.TaskInstanceExtendMapper;
import com.oppo.cloud.detect.service.TaskInstanceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@SpringBootTest(classes = TaskInstanceServiceImpl.class)
class TaskInstanceServiceImplTest {

    @MockBean(name = "taskInstanceExtendMapper")
    TaskInstanceExtendMapper taskInstanceExtendMapper;

    @SpyBean
    TaskInstanceService taskInstanceService;

    @Test
    void searchTaskStateHistory() {
        List<TaskStateHistory> taskStateHistories = new ArrayList<>();
        Mockito.when(taskInstanceExtendMapper.searchTaskState(Mockito.any())).thenReturn(taskStateHistories);
        List<TaskStateHistory> res =
                taskInstanceService.searchTaskStateHistory("project", "flow", "task", new Date(), new Date(), 2);
        Assertions.assertNotNull(res);
    }

    @Test
    void searchTaskDurationHistory() {
        List<TaskStateHistory> taskStateHistories = new ArrayList<>();
        TaskStateHistory taskStateHistory = new TaskStateHistory();
        taskStateHistory.setExecutionTime(new Date());
        taskStateHistory.setState("success");
        taskStateHistory.setValue(100d);
        taskStateHistories.add(taskStateHistory);
        Mockito.when(taskInstanceExtendMapper.searchTaskDuration(Mockito.any())).thenReturn(taskStateHistories);
        List<Double> res =
                taskInstanceService.searchTaskDurationHistory("project", "flow", "task", new Date(), new Date(), 2);
        Assertions.assertNotNull(res);

    }

    @Test
    void searchTaskRelativeEndTime() {
        List<TaskStateHistory> taskStateHistories = new ArrayList<>();
        TaskStateHistory taskStateHistory = new TaskStateHistory();
        taskStateHistory.setExecutionTime(new Date());
        taskStateHistory.setState("success");
        taskStateHistory.setValue(100d);
        taskStateHistories.add(taskStateHistory);
        Mockito.when(taskInstanceExtendMapper.searchTaskRelativeEndTime(Mockito.any())).thenReturn(taskStateHistories);
        List<Double> res =
                taskInstanceService.searchTaskRelativeEndTime("project", "flow", "task", new Date(), new Date(), 2);
        Assertions.assertNotNull(res);
    }
}
