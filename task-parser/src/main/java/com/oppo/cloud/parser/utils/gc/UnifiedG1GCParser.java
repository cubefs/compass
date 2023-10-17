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
import com.oppo.cloud.parser.utils.UnitUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support to parse jdk9+ g1 gc logs
 */
public class UnifiedG1GCParser implements GCLogParser {

    private static final String SIZE_CHANGE_TOKEN = "->";

    /**
     * 25M->11M(256M)
     */
    public static String MEMORY_SIZE_REGEX = "(?<before>[0-9]*\\.?[0-9]+)(?<beforeUnit>[KMG])->(?<after>[0-9]*\\.?[0-9]+)(?<afterUnit>[KMG])\\((?<total>[0-9]*\\.?[0-9]+)(?<totalUnit>[KMG])\\)";

    private static final Pattern MEMORY_SIZE_PATTERN = Pattern.compile(MEMORY_SIZE_REGEX);

    @Override
    public GCReport generateGCReport(InputStream in) throws IOException {
        GCReport gcReport = new GCReport();

        double maxheapUsedSize = 0;
        double maxheapAllocateSize = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (!line.contains(SIZE_CHANGE_TOKEN)) {
                continue;
            }
            Matcher heapSizeMatcher = MEMORY_SIZE_PATTERN.matcher(line);
            if (heapSizeMatcher.find()) {
                double before = UnitUtil.toKBByUnit(heapSizeMatcher.group("before"), heapSizeMatcher.group("beforeUnit"));
                double total = UnitUtil.toKBByUnit(heapSizeMatcher.group("total"), heapSizeMatcher.group("totalUnit"));
                maxheapUsedSize = Math.max(before, maxheapUsedSize);
                maxheapAllocateSize = Math.max(total, maxheapAllocateSize);
            }
        }

        gcReport.setMaxHeapUsedSize((int) Math.round(maxheapUsedSize));
        gcReport.setMaxHeapAllocatedSize((int) Math.round(maxheapAllocateSize));
        return gcReport;
    }


}
