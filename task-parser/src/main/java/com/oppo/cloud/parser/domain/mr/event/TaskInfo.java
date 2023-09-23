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

package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.Map;

@Data
public class TaskInfo {
    private String taskId;

    private long startTime;

    private long finishTime;

    private String taskType;

    private String splitLocations;

    private JhCounters counters;

    private String status;

    private String error;

    private String failedDueToAttemptId;

    private String successfulAttemptId;

    private Map<String, TaskAttemptInfo> attemptsMap;
}
