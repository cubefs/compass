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
 * Support to parse jdk8 g1 gc logs
 */
public class G1GCParser implements GCLogParser {

    /**
     * Eden: 2128.0M(2128.0M)->0.0B(2800.0M) Survivors: 49152.0K->65536.0K Heap: 2163.2M(14336.0M)->52707.3K(14336.0M)]
     */
    private static final String HEAP_REGEX = "Heap: (?<pre>[0-9]*\\.[0-9]*)(?<preUnit>[KMG])\\((?<preTotal>[0-9]*\\.[0-9]*)(?<preTotalUnit>[KMG])\\)->(?<post>[0-9]*\\.[0-9]*)(?<postUnit>[KMG])\\((?<total>[0-9]*\\.[0-9]*)(?<totalUnit>[KMG])\\)\\]";
    private static final Pattern HEAP_PATTERN = Pattern.compile(HEAP_REGEX);

    /**
     * 2023-09-18T12:10:39.771+0800: 5.049: [GC cleanup 135M->135M(14336M), 0.0013357 secs]
     */
    private static final String CLEANUP_REGEX = "(?<pre>[0-9]*\\.?[0-9]+)(?<preUnit>[KMG])->(?<post>[0-9]*\\.?[0-9]+)(?<postUnit>[KMG])\\((?<total>[0-9]*\\.?[0-9]+)(?<totalUnit>[KMG])\\)";
    private static final Pattern CLEANUP_PATTERN = Pattern.compile(CLEANUP_REGEX);

    private static final String HEAP_MATCHER = "Heap:";
    private static final String HEAP_CLEANUP_MATCHER = "cleanup";


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
            if (line.contains(HEAP_MATCHER)) {
                Matcher heapSizeMatcher = HEAP_PATTERN.matcher(line);
                if (heapSizeMatcher.find()) {
                    double pre = UnitUtil.toKBByUnit(heapSizeMatcher.group("pre"), heapSizeMatcher.group("preUnit"));
                    double total = UnitUtil.toKBByUnit(heapSizeMatcher.group("total"), heapSizeMatcher.group("totalUnit"));
                    maxheapUsedSize = Math.max(pre, maxheapUsedSize);
                    maxheapAllocateSize = Math.max(total, maxheapAllocateSize);
                }
            }
            if (line.contains(HEAP_CLEANUP_MATCHER)) {
                Matcher heapSizeMatcher = CLEANUP_PATTERN.matcher(line);
                if (heapSizeMatcher.find()) {
                    double pre = UnitUtil.toKBByUnit(heapSizeMatcher.group("pre"), heapSizeMatcher.group("preUnit"));
                    double total = UnitUtil.toKBByUnit(heapSizeMatcher.group("total"), heapSizeMatcher.group("totalUnit"));
                    maxheapUsedSize = Math.max(pre, maxheapUsedSize);
                    maxheapAllocateSize = Math.max(total, maxheapAllocateSize);
                }
            }
        }

        gcReport.setMaxHeapUsedSize((int) Math.round(maxheapUsedSize));
        gcReport.setMaxHeapAllocatedSize((int) Math.round(maxheapAllocateSize));
        return gcReport;
    }


}
