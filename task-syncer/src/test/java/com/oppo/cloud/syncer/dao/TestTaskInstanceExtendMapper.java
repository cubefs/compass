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

package com.oppo.cloud.syncer.dao;

import com.oppo.cloud.model.TaskInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ComponentScan(basePackages = "com.oppo.cloud")
public class TestTaskInstanceExtendMapper {

    @Autowired
    private TaskInstanceExtendMapper taskInstanceExtendMapper;

    @Test
    void testUpdateByCompositePrimaryKeySelective() {
        TaskInstance taskInstance = new TaskInstance();
        Map<String, Object> test = new HashMap<>();
        test.put("flow_name", "dag");
        test.put("task_name", "task");
        test.put("execution_time", "2022-04-19 16:15:43");
        taskInstance.setRetryTimes(111);
        taskInstance.setFlowName("dag");
        taskInstance.setTaskName("task");
        taskInstanceExtendMapper.updateByCompositePrimaryKeySelective(taskInstance);
    }
}
