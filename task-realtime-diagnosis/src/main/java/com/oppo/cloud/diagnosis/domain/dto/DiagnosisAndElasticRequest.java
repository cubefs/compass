package com.oppo.cloud.diagnosis.domain.dto;

import lombok.Data;

@Data
public class DiagnosisAndElasticRequest {
    private String jobName;
    private Boolean elastic;
    private String clusterId;
    private String namespace;
    private String clusterConfig;
}
