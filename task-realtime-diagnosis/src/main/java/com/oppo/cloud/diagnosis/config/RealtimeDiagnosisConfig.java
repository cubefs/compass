package com.oppo.cloud.diagnosis.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class RealtimeDiagnosisConfig {
    @Value("${diagnosis.monitorHost}")
    private String monitorHost = "";
    @Value("${diagnosis.monitorToken}")
    private String monitorToken = "";
    @Value("${diagnosis.monitorDatabase}")
    private String monitorDatabase = "";
}
