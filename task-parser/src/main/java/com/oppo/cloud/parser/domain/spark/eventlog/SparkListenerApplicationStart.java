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

import java.util.Map;

@Data
public class SparkListenerApplicationStart extends SparkListenerEvent {

    @JsonProperty("App Name")
    private String appName;
    @JsonProperty("App ID")
    private String appId;
    @JsonProperty("Timestamp")
    private Long time;
    @JsonProperty("User")
    private String sparkUser;
    @JsonProperty("App Attempt ID")
    private String appAttemptId;
    @JsonProperty("Driver Logs")
    private Map<String, String> driverLogs;
}
