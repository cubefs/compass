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
import com.oppo.cloud.common.domain.gc.HeapUsed;
import com.oppo.cloud.common.domain.gc.TenuredUsed;
import com.oppo.cloud.common.domain.gc.YoungUsed;
import com.tagtraum.perf.gcviewer.imp.DataReader;
import com.tagtraum.perf.gcviewer.imp.DataReaderFactory;
import com.tagtraum.perf.gcviewer.model.*;
import com.tagtraum.perf.gcviewer.util.MemoryFormat;
import com.tagtraum.perf.gcviewer.util.TimeFormat;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * gc日志解析
 */
@Slf4j
public class GCReportUtil {

    public static List<GCReport> generateGCReports(Map<Integer, InputStream> gcLogMap,
                                                   String logPath) throws IOException {
        if (gcLogMap == null) {
            return null;
        }
        List<GCReport> gcReports = new ArrayList<>();
        for (Map.Entry<Integer, InputStream> exeGc : gcLogMap.entrySet()) {
            GCReport gcReport = GCReportUtil.generateGCReport(exeGc.getValue());
            gcReport.setLogPath(logPath);
            gcReport.setExecutorId(exeGc.getKey());
            if (exeGc.getKey() == 0) {
                gcReport.setLogType(LogType.SPARK_DRIVER.getName());
            } else {
                gcReport.setLogType(LogType.SPARK_EXECUTOR.getName());
            }
            gcReports.add(gcReport);
        }
        return gcReports;
    }

    public static GCReport generateGCReport(InputStream in) throws IOException {
        GCResource gcResource = new GcResourceFile("");
        Logger logger =  Logger.getLogger("gcLog");
        logger.setLevel(Level.WARNING);
        gcResource.setLogger(logger);
        DataReader reader = new DataReaderFactory().getDataReader(gcResource, in);
        GCModel model = reader.read();

        NumberFormat footprintFormatter = new MemoryFormat();
        footprintFormatter.setMaximumFractionDigits(1);
        DateFormat totalTimeFormatter = new TimeFormat();
        GCReport gcReport = new GCReport();

        gcReport.setMaxHeapAllocatedSize(model.getHeapAllocatedSizes().getMax());
        gcReport.setMaxHeapUsedSize(model.getHeapUsedSizes().getMax());
        gcReport.setTotalTime(totalTimeFormatter.format(new Date((long) model.getRunningTime() * 1000L)));
        gcReport.setYoungGCCount(model.getGCPause().getN());
        gcReport.setYoungGCTime(model.getGCPause().getSum());
        gcReport.setFullGCCount(model.getFullGCPause().getN());
        gcReport.setFullGCTime(model.getFullGCPause().getSum());
        gcReport.setTotalGCCount(model.getPause().getN());
        gcReport.setTotalGCTime(model.getPause().getSum());

        List<HeapUsed> heapUsedList = new ArrayList<>();

        for (Iterator<AbstractGCEvent<?>> i = model.getEvents(); i.hasNext(); ) {
            AbstractGCEvent<?> event = i.next();
            if (event.getTotal() > 0) {
                long preTime = event.getDatestamp().toInstant().toEpochMilli();
                long postTime = preTime + (long) (event.getPause() * 1000);

                HeapUsed preUsed = new HeapUsed();
                preUsed.setTimestamp(preTime);
                preUsed.setUsed(event.getPreUsed());

                HeapUsed postUsed = new HeapUsed();
                postUsed.setTimestamp(postTime);
                postUsed.setUsed(event.getPostUsed());
                heapUsedList.add(preUsed);
                if (preTime != postTime) {
                    heapUsedList.add(postUsed);
                }

            }
        }

        List<YoungUsed> youngUsedList = new ArrayList<>();

        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext(); ) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
            if (abstractGCEvent instanceof GCEvent) {
                GCEvent event = (GCEvent) abstractGCEvent;
                GCEvent youngEvent = event.getYoung();
                if (youngEvent != null) {
                    if (youngEvent.getTotal() > 0) {
                        long preTime = event.getDatestamp().toInstant().toEpochMilli();
                        long postTime = preTime + (long) (event.getPause() * 1000);

                        YoungUsed preUsed = new YoungUsed();
                        preUsed.setTimestamp(preTime);
                        preUsed.setUsed(youngEvent.getPreUsed());

                        YoungUsed postUsed = new YoungUsed();
                        postUsed.setTimestamp(postTime);
                        postUsed.setUsed(youngEvent.getPostUsed());

                        youngUsedList.add(preUsed);
                        if (preTime != postTime) {
                            youngUsedList.add(postUsed);
                        }

                    }
                }
            }
        }

        Map<Long, HeapUsed> heapUsedMap = new LinkedHashMap<>();
        for (HeapUsed heapUsed : heapUsedList) {
            heapUsedMap.put(heapUsed.getTimestamp(), heapUsed);
        }
        Map<Long, YoungUsed> youngUsedMap = new LinkedHashMap<>();
        for (YoungUsed youngUsed : youngUsedList) {
            youngUsedMap.put(youngUsed.getTimestamp(), youngUsed);
        }

        List<TenuredUsed> tenuredUsedList = new ArrayList<>();
        for (Map.Entry<Long, HeapUsed> map : heapUsedMap.entrySet()) {
            if (youngUsedMap.containsKey(map.getKey())) {
                TenuredUsed tenuredUsed = new TenuredUsed();
                tenuredUsed.setTimestamp(map.getValue().getTimestamp());
                if (map.getValue().getUsed() > youngUsedMap.get(map.getKey()).getUsed()) {
                    tenuredUsed.setUsed(map.getValue().getUsed() - youngUsedMap.get(map.getKey()).getUsed());
                }
                tenuredUsedList.add(tenuredUsed);
            } else {
                youngUsedMap.put(map.getKey(), new YoungUsed(map.getKey(), null));
                tenuredUsedList.add(new TenuredUsed(map.getKey(), null));
            }
        }

        gcReport.setHeapUsed(new ArrayList<>(heapUsedMap.values()));
        gcReport.setYoungUsed(new ArrayList<>(youngUsedMap.values()));
        gcReport.setTenuredUsed(tenuredUsedList);

        return gcReport;
    }

}
