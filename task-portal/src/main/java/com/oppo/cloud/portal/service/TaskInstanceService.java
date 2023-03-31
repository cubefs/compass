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
     * 获取任务运行趋势图
     */
    List<MetricInfo> getJobDurationTrend(JobDetailRequest jobDetailRequest) throws Exception;

    /**
     * 获取任务基线图数据
     */
    Datum getJobDatum(JobDetailRequest jobDetailRequest) throws Exception;

    /**
     * 查询某个具体执行周期的聚合后的任务
     */
    TaskInstance searchTaskSum(String projectName, String flowName, String taskName,
                               Date executionTime);
}
