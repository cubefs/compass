package com.oppo.cloud.common.domain.mr.config;


import lombok.Data;

/**
 * 数据倾斜配置
 */
@Data
public class MRDataSkewConfig {

    private Boolean disable;

    private Double mapThreshold;

    private Double reduceThreshold;

    private Long duration;
}
