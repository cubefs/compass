package com.oppo.cloud.common.domain.mr.config;


import lombok.Data;

/**
 * 数据倾斜配置
 */
@Data
public class MRDataSkewConfig {

    private Boolean disable;

    private Long taskSize;
    /**
     * task duration(ms)
     */
    private Long taskDuration;

    private Double mapThreshold;

    private Double reduceThreshold;
    /**
     * app duration(ms)
     */
    private Long duration;
}
