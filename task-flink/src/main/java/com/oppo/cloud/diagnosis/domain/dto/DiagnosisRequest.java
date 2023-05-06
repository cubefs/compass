package com.oppo.cloud.diagnosis.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisRequest {
    private String appId;
    private LocalDateTime end;
    private LocalDateTime  start;
}
