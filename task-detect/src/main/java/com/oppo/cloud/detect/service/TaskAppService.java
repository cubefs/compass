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

package com.oppo.cloud.detect.service;

import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.detect.domain.AbnormalTaskAppInfo;

import java.util.List;
import java.util.Map;

/**
 * Task taskApp interface.
 */
public interface TaskAppService {

    /**
     * Get the result information of exception task Apps (including exception information).
     */
    AbnormalTaskAppInfo getAbnormalTaskAppsInfo(JobAnalysis jobAnalysis, String handledApps);

    /**
     * Get all AbnormalTaskApp data under the task, including data without taskApp.
     */
    Map<Integer, List<TaskApp>> getAbnormalTaskApps(JobAnalysis jobAnalysis);

    /**
     * Insert the app information of exception tasks.
     */
    void insertTaskApps(List<TaskApp> taskAppList) throws Exception;

    /**
     * Get the app information of exception tasks.
     */
    List<TaskApp> searchTaskApps(JobAnalysis jobAnalysis) throws Exception;
}
