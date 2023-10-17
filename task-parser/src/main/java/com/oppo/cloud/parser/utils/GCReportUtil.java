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

package com.oppo.cloud.parser.utils;

import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.parser.utils.gc.GCLogParserManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * gc日志解析
 */
@Slf4j
public class GCReportUtil {

    public static List<GCReport> generateGCReports(Map<Integer, byte[]> gcLogMap,
                                                   String logPath) throws Exception {
        if (gcLogMap == null) {
            return null;
        }
        List<GCReport> gcReports = new ArrayList<>();
        for (Map.Entry<Integer, byte[]> executorGc : gcLogMap.entrySet()) {
            GCReport gcReport = GCLogParserManager.generateGCReport(executorGc.getValue(), logPath);
            if (gcReport == null) {
                continue;
            }
            gcReport.setLogPath(logPath);
            gcReport.setExecutorId(executorGc.getKey());
            if (executorGc.getKey() == 0) {
                gcReport.setLogType(LogType.SPARK_DRIVER.getName());
            } else {
                gcReport.setLogType(LogType.SPARK_EXECUTOR.getName());
            }
            gcReports.add(gcReport);
        }
        return gcReports;
    }

}
