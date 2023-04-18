package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

@Getter
public enum ERealtimeTaskAppState {
    RUNNING(0, "RUNNING"),
    FINISHED(1, "FINISHED"),
            ;
    private final int code;
    private final String desc;

    ERealtimeTaskAppState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
