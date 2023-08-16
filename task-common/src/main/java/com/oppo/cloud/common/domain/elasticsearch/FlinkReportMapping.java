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

public class FlinkReportMapping extends Mapping{

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("properties", build()),
                new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                /* 属于某个作业的Id, 用于做索引 */
                new AbstractMap.SimpleEntry<>("flinkTaskAnalysisId", text()),
                /* 创建记录时间 */
                new AbstractMap.SimpleEntry<>("createTime", date())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
