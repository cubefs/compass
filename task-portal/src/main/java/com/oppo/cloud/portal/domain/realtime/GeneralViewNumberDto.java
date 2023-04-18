package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GeneralViewNumberDto {
    Integer baseTaskCntSum;
    Integer exceptionTaskCntSum;
    Integer resourceTaskCntSum;
    Integer totalCoreNumSum;
    Integer totalMemNumSum;
    Integer cutCoreNumSum;
    Integer cutMemNumSum;
    LocalDate date;
}
