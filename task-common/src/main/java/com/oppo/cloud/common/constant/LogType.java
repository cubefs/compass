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

    SCHEDULER("scheduler", "Scheduler Log"),
    SPARK_EVENT("event", "Spark Event Log"),
    SPARK_DRIVER("driver", "Spark Driver Log"),
    SPARK_EXECUTOR("executor", "Spark Executor Log"),
    YARN("yarn", "Yarn Diagnostics Log"),
    SPARK_GC("gc", "Spark GC Log"),
    MAPREDUCE_JOB_HISTORY("jobhistory", "MapReduce JobHistory Log"),
    MAPREDUCE_CONTAINER("mrContainer", "MapReduce Container Log");

    private final String name;

    private final String desc;

    private static final Map<String, LogType> MAP;

    LogType(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
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
