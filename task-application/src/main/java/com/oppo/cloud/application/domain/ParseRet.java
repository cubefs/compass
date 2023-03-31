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

package com.oppo.cloud.application.domain;

import com.oppo.cloud.application.constant.RetCode;
import com.oppo.cloud.model.TaskInstance;
import lombok.Data;

import java.util.Map;

/**
 * 解析结果
 */
@Data
public class ParseRet {

    /**
     * 返回码
     */
    private RetCode retCode;
    /**
     * 处理日志的实例
     */
    private TaskInstance taskInstance;
    /**
     * 原始列依赖数据
     */
    private Map<String, Object> rawData;

    public ParseRet(RetCode retCode, TaskInstance taskInstance) {
        this.retCode = retCode;
        this.taskInstance = taskInstance;
    }
}
