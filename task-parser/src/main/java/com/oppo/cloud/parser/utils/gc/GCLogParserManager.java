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

package com.oppo.cloud.parser.utils.gc;

import com.oppo.cloud.common.domain.gc.GCReport;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * jvm gc log parser manager,support to parse jdk8 cms/g1gc and jdk9+ g1gc
 */
@Slf4j
public class GCLogParserManager {

    private static final int LIMIT_LINE = 20;

    public static GCReport generateGCReport(byte[] bytes, String logPath) throws Exception {
        GCLogType gcType = getGCType(bytes);
        GCLogParser gcLogParser = createGCLogParser(gcType, logPath);
        if (gcLogParser == null) {
            return null;
        }
        return gcLogParser.generateGCReport(new ByteArrayInputStream(bytes));
    }

    private static GCLogParser createGCLogParser(GCLogType gcType, String logPath) {
        switch (gcType) {
            case CMS:
                return new CMSGCParser();
            case G1:
                return new G1GCParser();
            case UnifiedG1:
                return new UnifiedG1GCParser();
            case UNKNOWN:
                log.error("{} gc log type is unknown", logPath);
                break;
            default:
                break;
        }
        return null;
    }

    private static GCLogType getGCType(byte[] bytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        for (int i = 0; i < LIMIT_LINE; i++) {
            String line = br.readLine();
            sb.append(line).append(System.lineSeparator());
        }
        String gcLog = sb.toString();
        if (gcLog.contains("UseG1GC") || gcLog.contains("(young)")) {
            return GCLogType.G1;
        } else if (gcLog.contains("UseConcMarkSweepGC") || gcLog.contains("CMS")) {
            return GCLogType.CMS;
        } else if (gcLog.contains("Using G1")) {
            return GCLogType.UnifiedG1;
        } else {
            return GCLogType.UNKNOWN;
        }
    }

}
