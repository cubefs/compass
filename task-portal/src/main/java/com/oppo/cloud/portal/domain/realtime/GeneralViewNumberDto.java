package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GeneralViewNumberDto {
    Integer baseTaskCntSum = 0;
    Integer exceptionTaskCntSum = 0;
    Integer resourceTaskCntSum= 0;
    Integer totalCoreNumSum= 0;
    Integer totalMemNumSum= 0;
    Integer cutCoreNumSum= 0;
    Integer cutMemNumSum= 0;
    String date;
}
