package com.oppo.cloud.common.constant;

public enum MRTaskType {
    /**
     * MapReduce task type;
     */
    MAP("map"),
    REDUCE("reduce");

    private final String name;

    MRTaskType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
