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

package com.oppo.cloud.common.constant;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum LogGroupType {
    SCHEDULER("scheduler"),

    SPARK("spark"),
    MAPREDUCE("mapreduce"),

    YARN("yarn");

    private final String name;

    private static final Map<String, LogGroupType> MAP;
    LogGroupType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    static {
        Map<String, LogGroupType> map = new ConcurrentHashMap<>();
        for (LogGroupType instance : LogGroupType.values()) {
            map.put(instance.getName(), instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public static LogGroupType get(String name) {
        return MAP.get(name);
    }
}
