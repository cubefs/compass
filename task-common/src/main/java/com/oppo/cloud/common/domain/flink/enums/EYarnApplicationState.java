package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

/**
 * yarn app 状态
 */
@Getter
public enum EYarnApplicationState {
    // NEW, NEW_SAVING, SUBMITTED, ACCEPTED, RUNNING, FINISHED, FAILED, KILLED
    NEW(0, "NEW"),
    NEW_SAVING(1, "NEW_SAVING"),
    SUBMITTED(2, "SUBMITTED"),
    ACCEPTED(3, "ACCEPTED"),
    RUNNING(4, "RUNNING"),
    FINISHED(5, "FINISHED"),
    FAILED(6, "FAILED"),
    KILLED(7, "KILLED"),
    ;

    private final int code;
    private final String desc;

    EYarnApplicationState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
