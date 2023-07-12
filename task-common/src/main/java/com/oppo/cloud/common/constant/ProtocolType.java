package com.oppo.cloud.common.constant;

public enum ProtocolType {
    HDFS("hdfs");

    private final String name;
    ProtocolType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
