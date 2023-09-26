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
import com.oppo.cloud.detect.service.TaskInstanceService;
import com.oppo.cloud.detect.util.DetectorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Run the runtime duration anomaly detector.
 */
@Order(1)
@Service
@Slf4j
public class DurationAbnormal extends DetectServiceImpl {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Override
    public void detect(JobAnalysis jobAnalysis) throws Exception {
        // Failed tasks are not subject to runtime duration detection.
        if (jobAnalysis.getTaskState().equals(TaskStateEnum.fail.name())) {
            return;
        }
        double[] durationBeginAndEnd = getDurationBaseline(jobAnalysis);
        if (durationBeginAndEnd == null) {
            return;
        }
        // Extremely abnormal values.
        double normalDurationBegin = durationBeginAndEnd[0];
        double normalDurationEnd = durationBeginAndEnd[4];
        double tailAvg = jobAnalysis.getDuration();
        String normalDurationBeginStr = DetectorUtil.transferSecond(normalDurationBegin);
        String normalDurationEndStr = DetectorUtil.transferSecond(normalDurationEnd);
        jobAnalysis.setDurationBaseline(normalDurationEndStr);
        if ((tailAvg > normalDurationEnd || tailAvg < normalDurationBegin)) {
            jobAnalysis.getCategories().add(JobCategoryEnum.durationAbnormal.name());
        }
    }
}
