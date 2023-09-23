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

import com.oppo.cloud.detect.domain.TaskStateHistory;
import com.oppo.cloud.model.TaskInstance;

import java.util.Date;
import java.util.List;

/**
 * 任务运行实例接口
 */
public interface TaskInstanceService {

    /**
     * 查询近期任务执行情况数据
     */
    List<TaskStateHistory> searchTaskStateHistory(String projectName, String flowName, String taskName,
                                                  Date executionTime, Date endExecutionTime, Integer sampleNum);

    /**
     * 查询任务近期执行耗时数据
     */
    List<Double> searchTaskDurationHistory(String projectName, String flowName, String taskName,
                                           Date executionTime, Date endExecutionTime, Integer sampleNum);

    /**
     * 查询任务近期执行结束时间相对运行周期的差值数据
     */
    List<Double> searchTaskRelativeEndTime(String projectName, String flowName, String taskName,
                                           Date executionTime, Date endExecutionTime, Integer sampleNum);

    /**
     * 查询某个具体执行周期的聚合后的任务
     */
    TaskInstance searchTaskSum(String projectName, String flowName, String taskName,
                               Date executionTime);
}
