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

package com.oppo.cloud.parser.service.job.detector.spark;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.LargeTableScanAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.LargeTableScanConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.AccumulableInfo;
import com.oppo.cloud.parser.domain.spark.eventlog.SQLMetricInfo;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkListenerSQLExecutionStart;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkPlanInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LargeTableScanDetector implements IDetector {

    private final DetectorParam param;

    private final LargeTableScanConfig config;

    public LargeTableScanDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getLargeTableScanConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<LargeTableScanAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.LARGE_TABLE_SCAN.getCategory(), false);

        LargeTableScanAbnormal largeTableScanAbnormal = new LargeTableScanAbnormal();
        largeTableScanAbnormal.setAbnormal(false);

        Map<Long, AccumulableInfo> accumulableInfoMap = this.param.getReplayEventLogs().getAccumulableInfoMap();
        List<SparkListenerSQLExecutionStart> sqlExecutionStarts =
                this.param.getReplayEventLogs().getSqlExecutionStarts();
        List<Map<String, Object>> tables = new ArrayList<>();

        for (SparkListenerSQLExecutionStart sqlExecutionStart : sqlExecutionStarts) {
            SparkPlanInfo node = sqlExecutionStart.getSparkPlanInfo();
            List<SparkPlanInfo> queue = new ArrayList<>();
            queue.add(node);
            while (queue.size() != 0) {
                node = queue.remove(0);
                if (node.getNodeName().startsWith("Scan")) {
                    if (node.getMetrics() != null) {
                        for (SQLMetricInfo info : node.getMetrics()) {
                            AccumulableInfo accumulableInfo = accumulableInfoMap.get(info.getAccumulatorId());
                            if (accumulableInfo == null) {
                                continue;
                            }

                            if (accumulableInfo.getName().equals("number of output rows")) {
                                Long value = Long.valueOf(accumulableInfo.getValue().toString());

                                Map<String, Object> m = new HashMap<>();
                                m.put("table", getTableName(node.getNodeName()));
                                m.put("rows", value);
                                m.put("threshold", this.config.getScanTable());
                                m.put("abnormal", "false");

                                if (value >= this.config.getScanTable()
                                        && this.param.getAppDuration() > this.config.getDuration()) {
                                    largeTableScanAbnormal.setAbnormal(true);
                                    detectorResult.setAbnormal(true);
                                    m.put("abnormal", "true");
                                }
                                tables.add(m);

                            }
                        }
                    }
                }
                queue.addAll(node.getChildren());
            }
        }
        largeTableScanAbnormal.setTables(tables);
        detectorResult.setData(largeTableScanAbnormal);
        return detectorResult;
    }

    public String getTableName(String nodeName) {
        if (nodeName == null || nodeName.isEmpty()) {
            return "";
        }
        String[] fields = nodeName.split(" ");
        if (fields.length > 0) {
            return fields[fields.length - 1];
        }
        return "";
    }
}
