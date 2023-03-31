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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Properties;

@EqualsAndHashCode(callSuper = true)
@Data
public class SparkListenerEnvironmentUpdate extends SparkListenerEvent {

    @JsonProperty("JVM Information")
    private Properties jvmInformation;
    @JsonProperty("Spark Properties")
    private Properties sparkProperties;
    @JsonProperty("System Properties")
    private Properties systemProperties;
    @JsonProperty("Classpath Entries")
    private Properties classpathEntries;

    public String getJvmInformationProperty(String key) {
        return this.jvmInformation == null ? "" : this.jvmInformation.getProperty(key, "");
    }

    public String getSparkProperty(String key) {
        return this.sparkProperties == null ? "" : this.sparkProperties.getProperty(key, "");
    }

    public String getSystemProperty(String key) {
        return this.systemProperties == null ? "" : this.systemProperties.getProperty(key, "");
    }

    public String getClasspathEntry(String key) {
        return this.classpathEntries == null ? "" : this.classpathEntries.getProperty(key, "");
    }
}
