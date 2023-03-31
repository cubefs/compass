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

import com.oppo.cloud.common.domain.eventlog.config.MemWasteConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.service.ParamUtil;
import com.oppo.cloud.parser.service.rules.JobRulesConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MemWasteDetectorTest {

    @Resource
    JobRulesConfigService jobRulesConfigService;

    @Test
    void detect() throws Exception {
        MemWasteConfig memWasteConfig = jobRulesConfigService.detectorConfig.getMemWasteConfig();
        DetectorParam param = ParamUtil.getDetectorParam();
        MemWasteDetector detector = new MemWasteDetector(memWasteConfig);
        // DetectorResult detectorResult = detector.detect();
        // System.out.println(detectorResult);
    }

}
