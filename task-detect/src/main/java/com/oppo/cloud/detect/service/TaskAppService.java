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

import com.oppo.cloud.common.domain.elasticsearch.JobAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.detect.domain.AbnormalTaskAppInfo;

import java.util.List;
import java.util.Map;

/**
 * 任务taskApp接口
 */
public interface TaskAppService {

    /**
     * 获取异常任务Apps的结果信息（包括异常信息）
     */
    AbnormalTaskAppInfo getAbnormalTaskAppsInfo(JobAnalysis jobAnalysis, String handledApps);

    /**
     * 获取任务下所有的的AbnormalTaskApp数据，包括没有taskApp的数据
     */
    Map<Integer, List<TaskApp>> getAbnormalTaskApps(JobAnalysis jobAnalysis);

    /**
     * 往Es中插入异常任务的app信息
     */
    void insertTaskApps(List<TaskApp> taskAppList) throws Exception;

    /**
     * 从Es中获取异常任务的app信息
     */
    List<TaskApp> searchTaskApps(JobAnalysis jobAnalysis) throws Exception;
}
