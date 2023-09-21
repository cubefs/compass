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

package com.oppo.cloud.application.service.impl;

import com.oppo.cloud.application.domain.DelayedTaskInfo;
import com.oppo.cloud.application.service.DelayedTaskService;
import com.oppo.cloud.test.redis.WithRedisServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class DelayedTaskServiceImplTest implements WithRedisServer {

    @Autowired
    private DelayedTaskService delayedTaskService;

    @Test
    void pushDelayQueue() {
        DelayedTaskInfo delayedTaskInfo = new DelayedTaskInfo();
        delayedTaskInfo.setKey(UUID.randomUUID().toString());
        delayedTaskService.pushDelayedQueue(delayedTaskInfo);
    }

    @Test
    void getDelayTasks() {
        List<DelayedTaskInfo> delayedTaskInfoList = delayedTaskService.getDelayedTasks();
        log.info("delayTaskService:{}", delayedTaskInfoList);
    }
}
