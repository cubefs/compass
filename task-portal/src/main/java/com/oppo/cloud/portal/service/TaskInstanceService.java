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

package com.oppo.cloud.portal.service;

import com.oppo.cloud.common.domain.job.Datum;
import com.oppo.cloud.model.TaskInstance;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.domain.task.JobDetailRequest;

import java.util.Date;
import java.util.List;

public interface TaskInstanceService {

    /**
     * Get job duration trend
     */
    List<MetricInfo> getJobDurationTrend(JobDetailRequest jobDetailRequest) throws Exception;

    /**
     * Get job datum
     */
    Datum getJobDatum(JobDetailRequest jobDetailRequest) throws Exception;

    /**
     * Search task instance
     */
    TaskInstance searchTaskSum(String projectName, String flowName, String taskName,
                               Date executionTime);
}
