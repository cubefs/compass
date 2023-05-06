package com.oppo.cloud.diagnosis.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrafficThoughResponse {
    private String jobName;
    // 波谷建议tm个数
    private Integer trafficElasticTmNum;
    // 波谷开始时间
    private LocalDateTime trafficTroughStartTime;
    // 波谷结束时间
    private LocalDateTime trafficTroughEndTime;
}
