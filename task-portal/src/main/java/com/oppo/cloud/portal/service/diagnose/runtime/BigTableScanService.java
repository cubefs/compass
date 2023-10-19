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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectionStorage;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.LargeTableScanAbnormal;
import com.oppo.cloud.common.domain.eventlog.SpeculativeTaskAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.portal.domain.diagnose.Table;
import com.oppo.cloud.portal.domain.diagnose.runtime.BigTableScan;
import com.oppo.cloud.portal.domain.diagnose.runtime.TableData;
import com.oppo.cloud.portal.util.UnitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * BigTableScan Service
 */
@Service
public class BigTableScanService extends RunTimeBaseService<BigTableScan> {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public String getCategory() {
        return AppCategoryEnum.LARGE_TABLE_SCAN.getCategory();
    }

    @Override
    public BigTableScan generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        LargeTableScanAbnormal largeTableScanAbnormal =
                ((JSONObject) detectorResult.getData()).toJavaObject(LargeTableScanAbnormal.class);
        if (largeTableScanAbnormal.getTables().size() == 0) {
            return null;
        }
        BigTableScan bigTableScan = new BigTableScan();
        bigTableScan.setAbnormal(largeTableScanAbnormal.getAbnormal() != null && largeTableScanAbnormal.getAbnormal());
        Table<BigTableScan.TaskInfo> taskInfoTable = bigTableScan.getTable();
        List<BigTableScan.TaskInfo> taskInfoList = taskInfoTable.getData();
        // Variable values in the suggestion
        List<String> tables = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (Map<String, Object> map : largeTableScanAbnormal.getTables()) {
            BigTableScan.TaskInfo taskInfo = new BigTableScan.TaskInfo();
            taskInfo.setHiveTable((String) map.get("table"));
            taskInfo.setColumns(UnitUtil.transferRows(Double.parseDouble(String.valueOf(map.get("rows")))));
            taskInfo.setThreshold(UnitUtil.transferRows(config.getLargeTableScanConfig().getScanTable()));
            if ("true".equals(map.get("abnormal"))) {
                tables.add((String) map.get("table"));
                values.add(UnitUtil.transferRows(Double.parseDouble(String.valueOf(map.get("rows")))));
                taskInfoList.add(0, taskInfo);
            } else {
                taskInfoList.add(taskInfo);
            }
        }
        bigTableScan.getVars().put("tables", String.join(",", tables));
        bigTableScan.getVars().put("values", String.join(",", values));
        bigTableScan.getVars().put("threshold", UnitUtil.transferRows(config.getLargeTableScanConfig().getScanTable()));
        return bigTableScan;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("扫描表的行数超过%s行的任务", thresholdMap.getOrDefault("threshold", ""));
    }

    @Override
    public String generateItemDesc() {
        return "大表扫描分析";
    }

    @Override
    public String getType() {
        return "table";
    }
}
