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

package com.oppo.cloud.parser.domain.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogInfo;
import com.oppo.cloud.common.domain.job.LogRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务基本信息
 */
@Slf4j
@Data
public class TaskParam {

    /**
     * 任务类型
     */
    private String category;
    /**
     * job元数据
     */
    private LogRecord logRecord;
    /**
     * app元数据
     */
    private App app;
    /**
     * 日志信息
     */
    private LogInfo logInfo;

    /**
     * 任务重试次数
     */
    private int retry;

    private ObjectMapper objectMapper = new ObjectMapper();

    public TaskParam() {

    }

    public TaskParam(String category, LogRecord logRecord, App app, LogInfo logInfo) {
        this.category = category;
        this.logRecord = logRecord;
        this.app = app;
        this.logInfo = logInfo;
    }

}
