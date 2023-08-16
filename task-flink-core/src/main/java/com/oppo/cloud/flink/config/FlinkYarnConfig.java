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

package com.oppo.cloud.flink.config;

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
