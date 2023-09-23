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

import lombok.Data;

/**
 * 日志路径组成
 */
@Data
public class LogPathJoin {

    /**
     * 依赖的数据列
     */
    private String column;
    /**
     * 解析日志列正则
     */
    private String regex;
    /**
     * 匹配日志名称
     */
    private String name;
    /**
     * 静态数据，不需要正则匹配
     */
    private String data;
}
