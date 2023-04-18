package com.oppo.cloud.diagnosis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "flink.config.yarn")
@EnableConfigurationProperties(FlinkYarnConfig.class)
@Data
public class FlinkYarnConfig {
    private String parallel;
    private String tmCore;
    private String tmMemory;
    private String tmSlot;
    private String jmMemory;
    private String jobName;
}
