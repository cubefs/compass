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

package com.oppo.cloud.detect.detector;

import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.constant.TaskStateEnum;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * 任务首次失败检测器
 */
@Order(4)
@Service
public class FirstFailed extends DetectServiceImpl {

    @Override
    public void detect(JobAnalysis jobAnalysis) throws Exception {
        // 失败任务不进行运行时长检测
        if (jobAnalysis.getTaskState().equals(TaskStateEnum.fail.name())) {
            return;
        }
        if (jobAnalysis.getRetryTimes() > 1) {
            jobAnalysis.getCategories().add(JobCategoryEnum.firstFailed.name());
        }
    }
}
