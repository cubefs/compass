package com.oppo.cloud.diagnosis.advice.turning;

import com.oppo.cloud.common.domain.flink.enums.EDiagnosisTurningStatus;
import lombok.Data;

@Data
public class TurningAdvice {
    private Integer parallel;
    private Integer vcore;
    private Integer tmSlotNum;
    private Integer tmNum;
    private Integer totalCore;
    private Integer totalSlot;
    private Integer tmMem;
    // 满分100
    private Float score;
    private EDiagnosisTurningStatus status = EDiagnosisTurningStatus.NO_ADVICE;
    private String description = "";
}
