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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.application.domain.DelayedTaskInfo;
import com.oppo.cloud.application.service.DelayedTaskService;
import com.oppo.cloud.common.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class DelayedTaskServiceImpl implements DelayedTaskService {

    @Value("${custom.delayedTask.queue}")
    private String queue;

    @Value("${custom.delayedTask.processing}")
    private String processingKey;

    @Value("${custom.delayedTask.delayedSeconds}")
    private Integer delayedSeconds;

    @Autowired
    private RedisScript<Object> delayedTaskScript;

    @Autowired
    private RedisService redisService;

    /**
     * add delayed queue task
     */
    @Override
    public void pushDelayedQueue(DelayedTaskInfo delayedTaskInfo) {
        redisService.zSetAdd(queue, JSON.toJSONString(delayedTaskInfo), System.currentTimeMillis() + delayedSeconds * 1000);
        log.info("pushDelayedQueue:{},{}", queue, JSON.toJSONString(delayedTaskInfo));
    }

    /**
     * get delayed queue retry task
     */
    @Override
    public List<DelayedTaskInfo> getDelayedTasks() {
        Object delayTasks = redisService.executeScript(delayedTaskScript, Arrays.asList(queue, processingKey),
                String.valueOf(System.currentTimeMillis()));
        if (delayTasks == null) {
            return null;
        }
        JSONArray data = (JSONArray) JSON.parse((String) delayTasks);
        List<DelayedTaskInfo> taskList = new ArrayList<>();
        for (Object obj : data.toArray()) {
            DelayedTaskInfo delayedTaskInfo = JSONObject.parseObject((String) obj, DelayedTaskInfo.class);
            taskList.add(delayedTaskInfo);
        }
        return taskList;
    }
}
