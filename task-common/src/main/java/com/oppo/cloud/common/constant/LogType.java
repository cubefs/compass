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

public enum LogType {

    SCHEDULER("scheduler"),
    SPARK_EVENT("event"),
    SPARK_DRIVER("driver"),
    SPARK_EXECUTOR("executor"),
    YARN("yarn"),
    SPARK_GC("gc"),
    MAPREDUCE_JOB_HISTORY("jobhistory"),
    MAPREDUCE_CONTAINER("mrContainer");

    private final String name;

    private static final Map<String, LogType> MAP;

    LogType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    static {
        Map<String, LogType> map = new ConcurrentHashMap<>();
        for (LogType instance : LogType.values()) {
            map.put(instance.getName(), instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public static LogType get(String name) {
        return MAP.get(name);
    }

}
