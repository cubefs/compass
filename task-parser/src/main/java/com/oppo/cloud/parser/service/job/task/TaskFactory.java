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

package com.oppo.cloud.parser.service.job.task;

import com.oppo.cloud.parser.domain.job.TaskParam;


public class TaskFactory {


    public static Task create(TaskParam taskParam) {
        switch (taskParam.getCategory()) {
            case SCHEDULER:
                return new SchedulerTask(taskParam);
            case SPARK:
                return new SparkTask(taskParam);
            case MAPREDUCE:
                return new MapReduceTask(taskParam);
            case YARN:
                return new YarnTask(taskParam);
            default:
                return null;
        }
    }
}
