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
import com.oppo.cloud.common.domain.mr.MRLargeTableScanAbnormal;
import com.oppo.cloud.common.domain.mr.config.MRLargeTableScanConfig;
import com.oppo.cloud.parser.domain.job.MRDetectorParam;
import com.oppo.cloud.parser.domain.mr.CounterInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;

import java.util.Map;

public class MRLargeTableScanDetector implements IDetector {

    private final MRDetectorParam param;

    private final MRLargeTableScanConfig config;

    MRLargeTableScanDetector(MRDetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrLargeTableScanConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<MRLargeTableScanAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_LARGE_TABLE_SCAN.getCategory(), false);

        MRLargeTableScanAbnormal largeTableScanAbnormal = new MRLargeTableScanAbnormal();
        largeTableScanAbnormal.setAbnormal(false);

        Map<String, Long> group = param.getMrAppInfo().getTotalCounters().get(CounterInfo.CounterGroupName.HIVE.getCounterGroupName());
        if (group == null) {
            return null;
        }
        Long records = group.get(CounterInfo.CounterName.RECORDS_IN.getCounterName());
        if (records != null && records > config.getThreshold()) {
            largeTableScanAbnormal.setAbnormal(true);
            detectorResult.setAbnormal(true);
        }
        largeTableScanAbnormal.setRecords(records);
        detectorResult.setData(largeTableScanAbnormal);

        return detectorResult;
    }


}
