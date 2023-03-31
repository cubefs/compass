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

/**
 * Job表构建
 */
public class JobAnalysisMapping extends Mapping {

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("properties", build()),
                        new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                        /* 任务所属用户: [{userId: 23432, username: "someone"}] */
                        new AbstractMap.SimpleEntry<>("users", users()),
                        /* 项目名称 */
                        new AbstractMap.SimpleEntry<>("projectName", text()),
                        /* 项目ID */
                        new AbstractMap.SimpleEntry<>("projectId", digit("integer")),
                        /* 工作流名称 */
                        new AbstractMap.SimpleEntry<>("flowName", text()),
                        /* 工作流ID */
                        new AbstractMap.SimpleEntry<>("flowId", digit("integer")),
                        /* 任务名称 */
                        new AbstractMap.SimpleEntry<>("taskName", text()),
                        /* 任务ID */
                        new AbstractMap.SimpleEntry<>("taskId", digit("integer")),
                        /* 整个工作流开始执行时间 */
                        new AbstractMap.SimpleEntry<>("executionDate", date()),
                        /* 任务开始执行时间 */
                        new AbstractMap.SimpleEntry<>("startTime", date()),
                        /* 任务结束执行时间 */
                        new AbstractMap.SimpleEntry<>("endTime", date()),
                        /* 任务执行耗时 */
                        new AbstractMap.SimpleEntry<>("duration", digit("double")),
                        /* 任务执行状态: success、fail */
                        new AbstractMap.SimpleEntry<>("taskState", text()),
                        /* 运行所需要application memory·second */
                        new AbstractMap.SimpleEntry<>("memorySeconds", digit("double")),
                        /* 运行所需要application vcore·second */
                        new AbstractMap.SimpleEntry<>("vcoreSeconds", digit("double")),
                        /* 任务类型： SHELL, PYTHON, SPARK */
                        new AbstractMap.SimpleEntry<>("taskType", text()),
                        /* 失败重试次数 */
                        new AbstractMap.SimpleEntry<>("retryTimes", digit("integer")),
                        /* 异常类型，列表：["memoryWaste", "cpuWaste", ...] */
                        new AbstractMap.SimpleEntry<>("categories", text()),
                        /* 正常运行耗时区间 */
                        new AbstractMap.SimpleEntry<>("durationBaseline", text()),
                        /* 正常结束时间区间 */
                        new AbstractMap.SimpleEntry<>("endTimeBaseline", text()),
                        /* 最近一次成功时间(长期失败任务),普通时间类型 */
                        new AbstractMap.SimpleEntry<>("successExecutionDay", text()),
                        /* 距离最近一次成功的天数(长期失败任务) */
                        new AbstractMap.SimpleEntry<>("successDays", text()),
                        /* 任务使用内存(OOM预警) */
                        new AbstractMap.SimpleEntry<>("memory", digit("double")),
                        /* 内存占比(OOM预警) */
                        new AbstractMap.SimpleEntry<>("memoryRatio", digit("double")),
                        /* 是否删除 */
                        new AbstractMap.SimpleEntry<>("deleted", digit("integer")),
                        /* 任务处理状态：未处理(0)、已查看(1)、已处理(2) */
                        new AbstractMap.SimpleEntry<>("taskStatus", digit("integer")),
                        /* 记录创建时间 */
                        new AbstractMap.SimpleEntry<>("createTime", date()),
                        /* 记录更新时间 */
                        new AbstractMap.SimpleEntry<>("updateTime", date()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
