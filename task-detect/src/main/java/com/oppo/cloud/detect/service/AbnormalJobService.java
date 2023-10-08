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

import java.util.List;


public interface AbnormalJobService {

    /**
     * Update memory data and Vcore data.
     */
    void updateVcoreAndMemory(JobAnalysis jobAnalysis) throws Exception;

    /**
     * Incrementally update memory data and vCore data.
     */
    void updateResource(JobAnalysis jobAnalysis, List<TaskApp> taskAppList);

    /**
     * Search job
     */
    JobAnalysis searchJob(JobAnalysis jobAnalysis) throws Exception;

    /**
     * Insert or update job instances.
     */
    void insertOrUpdate(JobAnalysis jobAnalysis) throws Exception;

}
