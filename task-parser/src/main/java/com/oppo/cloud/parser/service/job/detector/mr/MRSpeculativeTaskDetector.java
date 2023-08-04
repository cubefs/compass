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

package com.oppo.cloud.parser.service.job.detector.mr;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.mr.MRSpeculativeAbnormal;
import com.oppo.cloud.common.domain.mr.config.MRSpeculativeTaskConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.mr.SpeculationInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MRSpeculativeTaskDetector implements IDetector {

    private final DetectorParam param;

    private final MRSpeculativeTaskConfig config;

    public MRSpeculativeTaskDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrSpeculativeTaskConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<MRSpeculativeAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_SPECULATIVE_TASK.getCategory(), false);

        SpeculationInfo speculationInfo = param.getMrAppInfo().getSpeculationInfo();
        if (speculationInfo.getTaskAttemptIds().size() < config.getThreshold()) {
            return null;
        }
        MRSpeculativeAbnormal mrSpeculativeAbnormal = new MRSpeculativeAbnormal();
        mrSpeculativeAbnormal.setAbnormal(true);
        mrSpeculativeAbnormal.setElapsedTime(speculationInfo.getElapsedTime());
        mrSpeculativeAbnormal.setTaskAttemptIds(speculationInfo.getTaskAttemptIds());
        detectorResult.setAbnormal(true);
        detectorResult.setData(mrSpeculativeAbnormal);

        return detectorResult;
    }


}
