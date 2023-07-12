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
