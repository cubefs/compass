package com.oppo.cloud.diagnosis.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class RealtimeDiagnosisConfig {
    /**
     * prometheus host
     */
    @Value("${diagnosis.flinkPrometheusHost}")
    private String flinkPrometheusHost = "";
    /**
     * prometheus token
     */
    @Value("${diagnosis.flinkPrometheusToken}")
    private String flinkPrometheusToken = "";
    /**
     * prometheus database
     */
    @Value("${diagnosis.flinkPrometheusDatabase}")
    private String flinkPrometheusDatabase = "";
}
