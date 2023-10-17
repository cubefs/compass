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
 * Support to parse jdk8 cms gc logs
 */
public class CMSGCParser implements GCLogParser {

    private static final String SIZE_CHANGE_TOKEN = "->";
    private static final String CMS_INITIAL_MARK_MATCHER = "CMS Initial Mark";
    private static final String ALLOCATION_FAILURE_MATCHER = "Allocation Failure";
    private static final String CMS_REMARK_MATCHER = "CMS Final Remark";
    private static final String FULL_GC_MATCHER = "Full GC";

    /**
     * 10208K(1048576K)] 596900K(1992320K)
     */
    public static String MEMORY_SIZE_REGEX = "(?<oldUsed>[0-9]*)(?<oldUsedUnit>[KMG])\\((?<oldTotal>[0-9]*)(?<oldTotalUnit>[KMG])\\)\\] "
            + "(?<heapUsed>[0-9]*)(?<heapUsedUnit>[KMG])\\((?<heapTotal>[0-9]*)(?<heapTotalUnit>[KMG])\\)";

    /**
     * 897412K->70278K(943744K), 0.0936951 secs] 904070K->80486K(1992321K)
     */
    public static String MEMORY_SIZE_TIME_REGEX = "(?<before>[0-9]*)(?<beforeUnit>[KMG])->(?<after>[0-9]*)(?<afterUnit>[KMG])\\((?<total>[0-9]*)(?<totalUnit>[KMG])\\), "
            + "(?<time>[0-9]*\\.[0-9]*) secs\\] "
            + "(?<heapBefore>[0-9]*)(?<heapBeforeUnit>[KMG])->(?<heapAfter>[0-9]*)(?<heapAfterUnit>[KMG])\\((?<heapTotal>[0-9]*)(?<heapTotalUnit>[KMG])\\)";

    public static Pattern MEMORY_SIZE_PATTERN = Pattern.compile(MEMORY_SIZE_REGEX);
    public static Pattern MEMORY_SIZE_TIME_PATTERN = Pattern.compile(MEMORY_SIZE_TIME_REGEX);


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
            if (line.contains(CMS_INITIAL_MARK_MATCHER) || line.contains(CMS_REMARK_MATCHER)) {
                Matcher matcher = MEMORY_SIZE_PATTERN.matcher(line);
                if (matcher.find()) {
                    double used = UnitUtil.toKBByUnit(matcher.group("heapUsed"), matcher.group("heapUsedUnit"));
                    double total = UnitUtil.toKBByUnit(matcher.group("heapTotal"), matcher.group("heapTotalUnit"));
                    maxheapUsedSize = Math.max(used, maxheapUsedSize);
                    maxheapAllocateSize = Math.max(total, maxheapAllocateSize);
                }
            } else if (line.contains(ALLOCATION_FAILURE_MATCHER) || line.contains(FULL_GC_MATCHER)) {
                Matcher matcher = MEMORY_SIZE_TIME_PATTERN.matcher(line);
                if (matcher.find()) {
                    double used = UnitUtil.toKBByUnit(matcher.group("heapBefore"), matcher.group("heapBeforeUnit"));
                    double total = UnitUtil.toKBByUnit(matcher.group("heapTotal"), matcher.group("heapTotalUnit"));
                    maxheapUsedSize = Math.max(used, maxheapUsedSize);
                    maxheapAllocateSize = Math.max(total, maxheapAllocateSize);
                }
            } else {
                Matcher matcher = MEMORY_SIZE_TIME_PATTERN.matcher(line);
                if (matcher.find()) {
                    double used = UnitUtil.toKBByUnit(matcher.group("heapBefore"), matcher.group("heapBeforeUnit"));
                    double total = UnitUtil.toKBByUnit(matcher.group("heapTotal"), matcher.group("heapTotalUnit"));
                    maxheapUsedSize = Math.max(used, maxheapUsedSize);
                    maxheapAllocateSize = Math.max(total, maxheapAllocateSize);
                }
            }
        }

        gcReport.setMaxHeapAllocatedSize((int) Math.round(maxheapAllocateSize));
        gcReport.setMaxHeapUsedSize((int) Math.round(maxheapUsedSize));

        return gcReport;
    }
}
