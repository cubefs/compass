package com.oppo.cloud.common.constant;

import lombok.Getter;

@Getter
public enum YarnAppType {
    /**
     * 任务状态
     */
    SPARK(0, "Apache Spark"),
    FLINK(1, "Apache Flink");

    private final String msg;
    private final Integer code;

    YarnAppType(int code, String msg) {

        this.msg = msg;
        this.code = code;
    }

}
