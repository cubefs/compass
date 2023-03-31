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

package com.oppo.cloud.common.domain.elasticsearch;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class LogSummaryMapping extends Mapping {

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("properties", build()),
                        new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                        /* applicationId名称 */
                        new AbstractMap.SimpleEntry<>("applicationId", text()),
                        /* 日志类型 */
                        new AbstractMap.SimpleEntry<>("logType", text()),
                        /* 项目名称 */
                        new AbstractMap.SimpleEntry<>("projectName", text()),
                        /* 工作流名称 */
                        new AbstractMap.SimpleEntry<>("flowName", text()),
                        /* 任务名称 */
                        new AbstractMap.SimpleEntry<>("taskName", text()),
                        /* 执行周期 */
                        new AbstractMap.SimpleEntry<>("executionDate", date()),
                        /* 任务重试次数 */
                        new AbstractMap.SimpleEntry<>("retryTimes", digit("integer")),
                        /* 日志异常类型 */
                        new AbstractMap.SimpleEntry<>("action", text()),
                        /* 日志解析步骤 */
                        new AbstractMap.SimpleEntry<>("step", digit("integer")),
                        /* 正则匹配的变量名 */
                        new AbstractMap.SimpleEntry<>("groupNames", text()),
                        /* 原始日志 */
                        new AbstractMap.SimpleEntry<>("rawLog", text()),
                        /* 原始日志路径 */
                        new AbstractMap.SimpleEntry<>("logPath", text()),
                        /* 正则匹配变量值 */
                        new AbstractMap.SimpleEntry<>("groupData", object()),
                        /* 日志时间 */
                        new AbstractMap.SimpleEntry<>("logTimestamp", digit("integer")),
                        /* 头匹配规则 */
                        new AbstractMap.SimpleEntry<>("heads", text()),
                        /* 中间匹配规则 */
                        new AbstractMap.SimpleEntry<>("middles", text()),
                        /* 尾匹配规则 */
                        new AbstractMap.SimpleEntry<>("tails", text()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
