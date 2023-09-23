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

public enum EventType {
    /**
     * MR event type
     */
    JOB_SUBMITTED,
    JOB_INITED,
    JOB_FINISHED,
    JOB_PRIORITY_CHANGED,
    JOB_STATUS_CHANGED,
    JOB_QUEUE_CHANGED,
    JOB_FAILED,
    JOB_KILLED,
    JOB_ERROR,
    JOB_INFO_CHANGED,
    TASK_STARTED,
    TASK_FINISHED,
    TASK_FAILED,
    TASK_UPDATED,
    NORMALIZED_RESOURCE,
    MAP_ATTEMPT_STARTED,
    MAP_ATTEMPT_FINISHED,
    MAP_ATTEMPT_FAILED,
    MAP_ATTEMPT_KILLED,
    REDUCE_ATTEMPT_STARTED,
    REDUCE_ATTEMPT_FINISHED,
    REDUCE_ATTEMPT_FAILED,
    REDUCE_ATTEMPT_KILLED,
    SETUP_ATTEMPT_STARTED,
    SETUP_ATTEMPT_FINISHED,
    SETUP_ATTEMPT_FAILED,
    SETUP_ATTEMPT_KILLED,
    CLEANUP_ATTEMPT_STARTED,
    CLEANUP_ATTEMPT_FINISHED,
    CLEANUP_ATTEMPT_FAILED,
    CLEANUP_ATTEMPT_KILLED,
    AM_STARTED
}