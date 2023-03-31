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

package com.oppo.cloud.syncer.domain;

import lombok.Data;

import java.util.List;

/**
 * 字段值映射
 */
@Data
public class ValueMapping {

    /**
     * 映射目标值
     */
    private String targetValue;
    /**
     * 映射目标值类型
     */
    private String targetType;
    /**
     * 映射原始值
     */
    private List<String> originValue;
    /**
     * 映射原始值类型
     */
    private String originType;
}
