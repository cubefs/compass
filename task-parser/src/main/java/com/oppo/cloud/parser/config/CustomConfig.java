/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.parser.config;

import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * custom yml config
 */
@Configuration
@ConfigurationProperties(prefix = "custom")
@Data
public class CustomConfig {

    /**
     * opensearch index prefix
     */
    @Value("${spring.opensearch.log-prefix}")
    private String logSummaryPrefix;

    @Value("${spring.opensearch.detector-prefix}")
    private String detectorPrefix;

    @Value("${spring.opensearch.gc-prefix}")
    private String gcPrefix;

    @Value("${spring.opensearch.job-prefix}")
    private String jobPrefix;

    @Value("${spring.opensearch.task-app-prefix}")
    private String taskAppPrefix;

    @Value("${custom.redisConsumer.enable}")
    private Boolean enableRedisConsumer;

    @Value("${custom.redisConsumer.maxThreadPoolSize}")
    private Integer maxThreadPoolSize;
    /**
     * logRecord list
     */
    @Value("${custom.redisConsumer.logRecordList}")
    private String logRecordList;

    /**
     * logRecord processing
     */
    @Value("${custom.redisConsumer.processingHash}")
    private String processingHash;


    private List<String> jvmType;

    public static final String GC_CONFIG = "gcConfig";

    @Bean(name = GC_CONFIG)
    public List<String> loadGCConfig() {
        return jvmType;
    }

    @Bean
    @ConfigurationProperties(prefix = "custom.detector")
    public DetectorConfig eventLogDetectorConfig() {
        return new DetectorConfig();
    }


}
