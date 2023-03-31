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

import com.oppo.cloud.model.TaskInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelayedTaskInfo {

    /**
     * 缓存key
     */
    private String key;
    /**
     * 重试次数
     */
    private Integer tryTimes;
    /**
     * 任务实例
     */
    private TaskInstance taskInstance;

    /**
     * 任务执行的原始数据
     */
    Map<String, String> rawData;
}
