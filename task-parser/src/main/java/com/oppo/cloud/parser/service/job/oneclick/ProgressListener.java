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

package com.oppo.cloud.parser.service.job.oneclick;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.domain.oneclick.OneClickProgress;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.common.util.spring.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * one click progress listener
 */
@Slf4j
public class ProgressListener implements IProgressListener {

    private final RedisService redisService;

    public ProgressListener() {

        redisService = (RedisService) SpringBeanUtil.getBean(RedisService.class);
    }

    @Override
    public void update(OneClickProgress oneClickProgress) {
        log.info("progressListener:{},{}", oneClickProgress.getLogType(), oneClickProgress);
        String key = String.format("%s:%s", oneClickProgress.getAppId(), oneClickProgress.getLogType().getName());
        try {
            redisService.set(key, JSONObject.toJSONString(oneClickProgress));
        } catch (Exception e) {
            log.error("Exception:", e);
        }
    }
}
