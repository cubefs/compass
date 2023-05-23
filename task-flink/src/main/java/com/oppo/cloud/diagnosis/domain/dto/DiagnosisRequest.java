package com.oppo.cloud.diagnosis.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisRequest {
    /**
     * app id
     */
    private String appId;
    /**
     * 时间戳秒
     */
    private Long end;
    /**
     * 时间戳秒
     */
    private Long  start;
}
