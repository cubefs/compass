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

package com.oppo.cloud.portal.service.diagnose.runtime;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.mr.MRLargeTableScanAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Table;
import com.oppo.cloud.portal.domain.diagnose.runtime.mr.MRBigTableScan;
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MR大表扫描
 */
@Order(4)
@Service
public class MRBigTableScanService extends RunTimeBaseService<MRBigTableScan> {
    @Override
    public String getCategory() {
        return AppCategoryEnum.MR_LARGE_TABLE_SCAN.getCategory();
    }

    @Override
    public MRBigTableScan generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        MRLargeTableScanAbnormal largeTableScanAbnormal =
                ((JSONObject) detectorResult.getData()).toJavaObject(MRLargeTableScanAbnormal.class);

        MRBigTableScan bigTableScan = new MRBigTableScan();
        bigTableScan.setAbnormal(largeTableScanAbnormal.getAbnormal() != null && largeTableScanAbnormal.getAbnormal());
        Table<MRBigTableScan.TaskInfo> taskInfoTable = bigTableScan.getTable();
        List<MRBigTableScan.TaskInfo> taskInfoList = taskInfoTable.getData();
        MRBigTableScan.TaskInfo taskInfo = new MRBigTableScan.TaskInfo();
        String value = UnitUtil.transferRows(Double.parseDouble(String.valueOf(largeTableScanAbnormal.getRecords())));
        taskInfo.setColumns(value);
        String threshold = UnitUtil.transferRows(config.getMrLargeTableScanConfig().getThreshold());
        taskInfo.setThreshold(threshold);
        taskInfoList.add(taskInfo);
        bigTableScan.getVars().put("values", value);
        bigTableScan.getVars().put("threshold", threshold);
        return bigTableScan;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("扫描表的行数超过%s行的任务", thresholdMap.getOrDefault("threshold", ""));
    }

    @Override
    public String generateItemDesc() {
        return "MR大表扫描分析";
    }

    @Override
    public String getType() {
        return "table";
    }
}
