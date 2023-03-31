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

package com.oppo.cloud.common.domain.eventlog.config;

import lombok.Data;

/**
 * 内存浪费配置
 */
@Data
public class SpeculativeTaskConfig {

    private Boolean disable;
    /**
     * 推测执行Task数量
     */
    private Integer threshold;
    /**
     * 过滤时间(ms)
     */
    private Long duration;

}
