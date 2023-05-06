package com.oppo.cloud.diagnosis.domain.dto;

import lombok.Data;

@Data
public class ElasticRequest {
    private Integer tmNum;
    private String jobName;
    private String clusterId;
    private String namespace;
    private String clusterConfig;
}
