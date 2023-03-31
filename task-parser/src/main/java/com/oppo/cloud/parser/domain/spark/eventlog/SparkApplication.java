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

package com.oppo.cloud.parser.domain.spark.eventlog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oppo.cloud.parser.utils.UnitUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Data
public class SparkApplication {

    private Long appDuration;
    private Long appStartTimestamp;
    private Long appEndTimestamp;
    private Properties jvmInformation;
    private Properties sparkProperties;
    private Properties systemProperties;
    private Properties classpathEntries;

    private Long executorMemory;

    private Long driverMemory;

    private String sparkExecutorCores;

    public SparkApplication() {

    }

    /**
     * SparkListenerEnvironmentUpdate属性
     */
    public void setSparkApplication(SparkListenerEnvironmentUpdate env) {
        this.jvmInformation = env.getJvmInformation();
        this.sparkProperties = env.getSparkProperties();
        this.systemProperties = env.getSystemProperties();
        this.classpathEntries = env.getClasspathEntries();
        this.executorMemory = UnitUtil.toBytes(env.getSparkProperty("spark.executor.memory"));
        this.driverMemory = UnitUtil.toBytes(env.getSparkProperty("spark.driver.memory"));
        this.sparkExecutorCores = env.getSparkProperty("spark.executor.cores");
    }

    public Long getAppDuration() {
        return this.appEndTimestamp - this.appStartTimestamp;
    }
}
