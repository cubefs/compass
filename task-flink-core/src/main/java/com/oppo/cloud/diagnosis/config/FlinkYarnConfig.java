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
    private String parallel = "parallelism.default";
    private String tmCore = "yarn.containers.vcores";
    private String tmMemory = "taskmanager.memory.process.size";
    private String tmSlot = "taskmanager.numberOfTaskSlots";
    private String jmMemory = "jobmanager.memory.process.size";
    private String jobName = "yarn.application.name";
}
