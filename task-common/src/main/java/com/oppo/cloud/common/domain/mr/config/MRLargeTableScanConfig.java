package com.oppo.cloud.common.domain.mr.config;

import lombok.Data;

@Data
public class MRLargeTableScanConfig {
    private Boolean disable;

    private Double threshold;

    private Long duration;

}
