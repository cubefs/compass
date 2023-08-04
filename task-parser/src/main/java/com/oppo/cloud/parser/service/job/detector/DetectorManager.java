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

package com.oppo.cloud.parser.service.job.detector;

import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DetectorManager implements IManager {

    @Override
    public List<IDetector> createDetectors(DetectorParam param) {
        return new DetectorRegister(param).registerDetectors();
    }

    @Override
    public DetectorStorage run(DetectorParam param) {
        List<IDetector> detectors = this.createDetectors(param);
        DetectorStorage detectorStorage = new DetectorStorage(
                param.getFlowName(), param.getProjectName(),
                param.getTaskName(), param.getExecutionTime(),
                param.getTryNumber(), param.getAppId(),
                param.getLogPath(), param.getConfig());

        for (IDetector detector : detectors) {
            DetectorResult result;
            try {
                result = detector.detect();
            } catch (Exception e) {
                log.error("Exception:{},", param.getAppId(), e);
                continue;
            }
            if (result == null) {
                continue;
            }
            if (result.getAbnormal()) {
                detectorStorage.setAbnormal(true);
                log.info("DetectorResult:{},{}", param.getAppId(), result.getAppCategory());
            }
            detectorStorage.addDetectorResult(result);
        }

        return detectorStorage;
    }


}
