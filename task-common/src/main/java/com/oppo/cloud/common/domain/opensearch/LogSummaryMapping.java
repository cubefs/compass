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

package com.oppo.cloud.common.domain.opensearch;

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
                        /* applicationId */
                        new AbstractMap.SimpleEntry<>("applicationId", text()),
                        /* log type */
                        new AbstractMap.SimpleEntry<>("logType", text()),
                        /* project name */
                        new AbstractMap.SimpleEntry<>("projectName", text()),
                        /* flow name */
                        new AbstractMap.SimpleEntry<>("flowName", text()),
                        /* task name */
                        new AbstractMap.SimpleEntry<>("taskName", text()),
                        /* execution date */
                        new AbstractMap.SimpleEntry<>("executionDate", date()),
                        /* retry times */
                        new AbstractMap.SimpleEntry<>("retryTimes", digit("integer")),
                        /* action type */
                        new AbstractMap.SimpleEntry<>("action", text()),
                        /* step */
                        new AbstractMap.SimpleEntry<>("step", digit("integer")),
                        /* group names */
                        new AbstractMap.SimpleEntry<>("groupNames", text()),
                        /* raw log */
                        new AbstractMap.SimpleEntry<>("rawLog", text()),
                        /* log path */
                        new AbstractMap.SimpleEntry<>("logPath", text()),
                        /* group data */
                        new AbstractMap.SimpleEntry<>("groupData", object()),
                        /* log timestamp */
                        new AbstractMap.SimpleEntry<>("logTimestamp", digit("integer")),
                        /* heads rule */
                        new AbstractMap.SimpleEntry<>("heads", text()),
                        /* middles rule */
                        new AbstractMap.SimpleEntry<>("middles", text()),
                        /* tails rule */
                        new AbstractMap.SimpleEntry<>("tails", text()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
