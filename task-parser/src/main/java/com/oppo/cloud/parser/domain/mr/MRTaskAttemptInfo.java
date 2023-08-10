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

package com.oppo.cloud.parser.domain.mr;

import lombok.Data;

import java.util.Map;

@Data
public class MRTaskAttemptInfo {

    private int taskId;

    private String attemptId;

    private String taskStatus;
    private long startTime;

    private long finishTime;

    private long shuffleFishTime;

    private long sortFinishTime;

    private long elapsedTime;

    private String error;

    private Map<String, Map<String, Long>> counters;
}
