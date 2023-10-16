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

import java.io.*;

/**
 * jvm gc log parser manager,support to parse jdk8 cms/g1gc and jdk9+ g1gc
 */
public class GCLogParserManager {

    private static final int LIMIT_LINE = 20;

    public static GCReport generateGCReport(byte[] bytes) throws Exception {
        GCLogParser gcLogParser = createGCLogParser(readLines(bytes));
        return gcLogParser.generateGCReport(new ByteArrayInputStream(bytes));
    }

    private static GCLogParser createGCLogParser(String s) {
        if (s.contains("UseG1GC") || s.contains("(young)")) {
            return new G1GCParser();
        } else if (s.contains("UseConcMarkSweepGC") || s.contains("CMS")) {
            return new CMSGCParser();
        } else if (s.contains("Using G1")) {
            return new UnifiedG1GCParser();
        }
        return new G1GCParser();
    }

    private static String readLines(byte[] bytes) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        for (int i = 0; i < LIMIT_LINE; i++) {
            String line = br.readLine();
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
