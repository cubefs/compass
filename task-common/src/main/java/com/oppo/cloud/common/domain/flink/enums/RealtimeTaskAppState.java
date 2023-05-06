package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

/**
 * 实时作业运行状态
 */
@Getter
public enum RealtimeTaskAppState {
    /**
     * 运行状态
     */
    RUNNING(0, "RUNNING"),
    /**
     * 结束状态
     */
    FINISHED(1, "FINISHED"),
    ;
    private final int code;
    private final String desc;

    RealtimeTaskAppState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
