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
import com.oppo.cloud.common.domain.eventlog.OOMAbnormal;
import com.oppo.cloud.common.domain.eventlog.OOMTableInfo;
import com.oppo.cloud.common.domain.eventlog.config.OOMWarnConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.*;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class OOMWarnDetector implements IDetector {

    private final DetectorParam param;

    private final OOMWarnConfig config;

    public OOMWarnDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getOomWarnConfig();
    }

    @Override
    public DetectorResult detect() {
        Map<Long, AccumulableInfo> accumulableInfoMap = this.param.getReplayEventLogs().getAccumulableInfoMap();
        Map<Long, Long> driverUpdateMap = this.param.getReplayEventLogs().getDriverUpdateMap();
        List<SparkListenerSQLExecutionStart> sqlExecutionStarts =
                this.param.getReplayEventLogs().getSqlExecutionStarts();

        long maxMemory = 0L;
        long maxRows = 0L;
        StringBuilder scanNodeTable = new StringBuilder();
        HashSet<Long> broadcastExchangeSet = new HashSet<>();
        List<OOMTableInfo> tables = new ArrayList<>();

        for (SparkListenerSQLExecutionStart sqlExecutionStart : sqlExecutionStarts) {
            if (sqlExecutionStart.getDescription().startsWith("collectAsList")) {
                continue;
            }
            SparkPlanInfo node = sqlExecutionStart.getSparkPlanInfo();
            List<SparkPlanInfo> queue = new ArrayList<>();
            queue.add(node);
            long sumMemory = 0L;
            long nodeMaxRows = 0L;
            while (queue.size() != 0) {
                node = queue.remove(0);
                if (node.getNodeName().startsWith("BroadcastExchange")) {
                    DetectResult detectResult = new DetectResult();

                    long rows = 0L;
                    if (detectBroadcastOOM(node, detectResult)) {
                        AccumulableInfo accumulableInfo = accumulableInfoMap.getOrDefault(detectResult.getMaxResult(),
                                null);
                        if (accumulableInfo != null) {
                            rows = Long.parseLong(accumulableInfo.getValue().toString());
                            nodeMaxRows = Math.max(rows, nodeMaxRows);
                        }
                    }

                    if (node.getMetrics() != null) {
                        for (SQLMetricInfo info : node.getMetrics()) {
                            if (!info.getNodeName().startsWith("data size")) {
                                continue;
                            }
                            if (broadcastExchangeSet.contains(info.getAccumulatorId())) {
                                continue;
                            }
                            broadcastExchangeSet.add(info.getAccumulatorId());
                            Long val = driverUpdateMap.get(info.getAccumulatorId());
                            if (val == null) {
                                continue;
                            }
                            sumMemory += val;
                            String table = detectResult.getScanNodeTable();
                            if (rows > 0) {
                                OOMTableInfo oomTableInfo = new OOMTableInfo();
                                oomTableInfo.setTable(table);
                                oomTableInfo.setRows(rows);
                                oomTableInfo.setMemory(val);
                                tables.add(oomTableInfo);
                            }
                            scanNodeTable.append(table).append(",");
                        }
                    }
                }
                queue.addAll(node.getChildren());
            }

            maxMemory = Math.max(sumMemory, maxMemory);
            maxRows = Math.max(nodeMaxRows, maxRows);
        }

        String scanNodeTableStr = "";
        if (scanNodeTable.length() > 1) {
            scanNodeTableStr = scanNodeTable.deleteCharAt(scanNodeTable.length() - 1).toString();
        }

        return judgeOOM(maxMemory, maxRows, scanNodeTableStr, tables);
    }

    public DetectorResult judgeOOM(Long sumMemory, Long maxRows, String scanNodeTable, List<OOMTableInfo> tables) {
        DetectorResult<OOMAbnormal> detectorResult = new DetectorResult<>(AppCategoryEnum.OOMWarn.getCategory(), false);

        OOMAbnormal abnormal = new OOMAbnormal();

        abnormal.setTables(tables);

        SparkApplication application = this.param.getReplayEventLogs().getApplication();

        if (sumMemory != null) {
            float driverUsePercent = sumMemory.floatValue() / application.getDriverMemory() * 100;

            int driverThresholdType = isOverThreshold(driverUsePercent, maxRows);
            if (application.getDriverMemory() != 0 && driverThresholdType < 2) {
                Map<String, String> actionData = getActionData(driverThresholdType);
                String usePercent = String.format("%.2f", driverUsePercent);
                Map<String, String> varsMap = getVars("driver", actionData.get("action"),
                        application.getDriverMemory(), application.getExecutorMemory(),
                        sumMemory, scanNodeTable, usePercent, maxRows);

                abnormal.setVars(varsMap);
                detectorResult.setAbnormal(true);
                detectorResult.setData(abnormal);
                return detectorResult;
            }
            float executorUsePercent = sumMemory.floatValue() / application.getExecutorMemory().floatValue() * 100;
            int executorThresholdType = isOverThreshold(executorUsePercent, maxRows);
            if (application.getExecutorMemory() != 0 && executorThresholdType < 2) {
                Map<String, String> actionData = getActionData(executorThresholdType);

                String usePercent = String.format("%.2f", executorUsePercent);

                Map<String, String> varsMap = getVars("executor", actionData.get("action"),
                        application.getDriverMemory(), application.getExecutorMemory(),
                        sumMemory, scanNodeTable, usePercent, maxRows);

                detectorResult.setAbnormal(true);
                abnormal.setVars(varsMap);
                detectorResult.setData(abnormal);
                return detectorResult;
            }
        }
        detectorResult.setData(abnormal);
        return detectorResult;
    }

    /**
     * 获取基本数据
     */
    public Map<String, String> getActionData(int thresholdType) {
        Map<String, String> m = new HashMap<>();
        switch (thresholdType) {
            case 0:
                m.put("action", "broadcastOOM");
                m.put("threshold", String.valueOf(this.config.getBroadcastRows()));
                break;
            case 1:
                m.put("action", "oom");
                m.put("threshold", String.valueOf(this.config.getOom()));
                break;
            default:
                break;
        }
        return m;
    }

    /**
     * 获取vars数据
     */
    public Map<String, String> getVars(String execType, String action, Long driverMemory, Long executorMemory,
                                       Long useMemory, String scanNodeTable, String usePercent, Long maxRows) {
        Map<String, String> m = new HashMap<>();
        m.put("execType", execType);
        m.put("action", action);
        m.put("driverMemory", String.valueOf(driverMemory));
        m.put("executorMemory", String.valueOf(executorMemory));
        m.put("useMemory", String.valueOf(useMemory));
        m.put("maxRows", String.valueOf(maxRows));
        m.put("usePercent", usePercent);
        m.put("scanTable", scanNodeTable);
        return m;
    }

    /**
     * 使用内存超过阈值，或者广播过滤数据行数是否超过阈值
     */
    public int isOverThreshold(float usePercent, Long maxRows) {
        // 广播过滤数据行数过多
        if (((maxRows >= this.config.getBroadcastRows()) && (usePercent >= this.config.getBroadcastRowsOom())) &&
                this.param.getAppDuration() > this.config.getDuration()) {
            return 0;
        }
        // 内存超过阈值
        if (usePercent >= this.config.getOom() && this.param.getAppDuration() > this.config.getDuration()) {
            return 1;
        }
        return 2;
    }

    public boolean detectBroadcastOOM(SparkPlanInfo planInfo, DetectResult maxDetectResult) {
        if (planInfo.getNodeName().startsWith("Filter")) {
            if (planInfo.getMetrics() != null) {
                for (SQLMetricInfo metricInfo : planInfo.getMetrics()) {
                    if (metricInfo.getNodeName() == null) {
                        continue;
                    }
                    if (metricInfo.getNodeName().equals("number of output rows")) {
                        maxDetectResult.setMaxResult(metricInfo.getAccumulatorId());
                    }
                }
            }
        } else if (planInfo.getNodeName().startsWith("Scan")) {
            maxDetectResult.setSparkPlanInfo(planInfo);
            return true;
        }
        for (SparkPlanInfo children : planInfo.getChildren()) {
            if (detectBroadcastOOM(children, maxDetectResult)) {
                return true;
            }
        }
        return false;
    }
}
