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

package com.oppo.cloud.flink.service.impl;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.config.FlinkDiagnosisConfig;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.flink.constant.MonitorMetricConstant;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oppo.cloud.common.domain.flink.enums.DiagnosisParam.*;
import static com.oppo.cloud.flink.constant.MonitorMetricConstant.*;


/**
 * Diagnostic metrics acquisition module.
 */
@Slf4j
@Service
public class FlinkDiagnosisMetricsServiceImpl {
    @Autowired
    DiagnosisParamsConstants cons;

    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Autowired
    FlinkDiagnosisConfig flinkDiagnosisConfig;

    public double getJobMetricCurrentValueMax(String promQl) {
        try {
            List<MetricResult.DataResult> dataResults = getJobMetrics(promQl);
            return monitorMetricUtil.getLatest(dataResults);
        } catch (Exception e) {
            log.error("Failed to execute PromQL.");
            log.error(e.getMessage() + promQl, e);
        }
        return 0D;
    }

    public double getJobMetricCurrentValueMin(String promQl) {
        try {
            List<MetricResult.DataResult> dataResults = getJobMetrics(promQl);
            return monitorMetricUtil.getMin(dataResults);
        } catch (Exception e) {
            log.error("Failed to execute PromQL.");
            log.error(e.getMessage() + promQl, e);
        }
        return 0D;
    }

    public double getJobMetricCurrentValueAvg(String promQl) {
        try {
            List<MetricResult.DataResult> dataResults = getJobMetrics(promQl);
            return monitorMetricUtil.getAvg(dataResults);
        } catch (Exception e) {
            log.error("Failed to execute PromQL.");
            log.error(e.getMessage() + promQl, e);
        }
        return 0D;
    }

    /**
     * Query for metrics data.
     *
     * @param promQl PromQL query statement.
     * @param start  Start timestamp in seconds.
     * @param end    End timestamp in seconds.
     * @return Query result.
     */
    public List<MetricResult.DataResult> getJobMetrics(String promQl, long start, long end) {
        int step = monitorMetricUtil.getStep(start, end);
        return getJobMetrics(promQl, start, end, step);
    }

    public List<MetricResult.DataResult> getJobManagerMetrics(String promQl, DiagnosisContext context, long start, long end) {
        String jobUpTime = addLabel(promQl, "job_id", context.getMessages().get(JobId).toString());
        return getJobMetrics(jobUpTime, start, end);
    }

    public List<MetricResult.DataResult> getJobMetrics(String promQl, DiagnosisContext context, long start, long end) {
        String jobUpTime = addLabel(promQl, "job", context.getMessages().get(Job).toString());
        return getJobMetrics(jobUpTime, start, end);
    }

    public List<MetricResult.DataResult> getTaskManagerMetrics(String promQl, DiagnosisContext context, long start, long end) {
        List<MetricResult.DataResult> res = new ArrayList<>();
        List<String> tmIds = (List<String>) context.getMessages().get(TmIds);
        if (tmIds != null && tmIds.size() > 0) {
            for (int i = 0; i < Math.ceil((float) tmIds.size() / 5); i++) {
                int from = i * 5;
                int to = i * 5 + 5;
                if (to > tmIds.size()) {
                    to = tmIds.size();
                }
                String promQlWithLabel = addLabel(promQl, "tm_id", StringUtils.join(tmIds.subList(from, to), '|'));
                List<MetricResult.DataResult> jobMetrics = getJobMetrics(promQlWithLabel, start, end);
                res.addAll(jobMetrics);
            }
        }
        return res;
    }

    public List<MetricResult.DataResult> getTaskManagerMetrics(String promQl, DiagnosisContext context, long start, long end, int step) {
        List<MetricResult.DataResult> res = new ArrayList<>();
        List<String> tmIds = (List<String>) context.getMessages().get(TmIds);
        if (tmIds != null && tmIds.size() > 0) {
            for (int i = 0; i < Math.ceil((float) tmIds.size() / 5); i++) {
                int from = i * 5;
                int to = i * 5 + 5;
                if (to > tmIds.size()) {
                    to = tmIds.size();
                }
                String promQlWithLabel = addLabel(promQl, "tm_id", StringUtils.join(tmIds.subList(from, to), '|'));
                List<MetricResult.DataResult> jobMetrics = getJobMetrics(promQlWithLabel, start, end, step);
                if (jobMetrics != null) {
                    res.addAll(jobMetrics);
                }
            }
        }
        return res;
    }

    /**
     * Query for metrics data.
     *
     * @param promQl PromQL query statement.
     * @param start  Start timestamp in seconds.
     * @param end    End timestamp in seconds.
     * @param step   Step in seconds.
     * @return Query result.
     */
    public List<MetricResult.DataResult> getJobMetrics(String promQl, long start, long end, int step) {
        String queryUrl = "";
        try {
            if (flinkDiagnosisConfig.getFlinkPrometheusToken() != null && !flinkDiagnosisConfig.getFlinkPrometheusToken().equals("")) {
                promQl = addLabel(promQl, "__TOKEN__", flinkDiagnosisConfig.getFlinkPrometheusToken());
            }
            if (flinkDiagnosisConfig.getFlinkPrometheusDatabase() != null && !flinkDiagnosisConfig.getFlinkPrometheusDatabase().equals("")) {
                promQl = addLabel(promQl, "__DATABASE__", flinkDiagnosisConfig.getFlinkPrometheusDatabase());
            }
            promQl = promQl.replace("$__interval_s", String.format("%ds", step));
            promQl = promQl.replace("$__interval", String.format("%d", step));
            // The PromQL query may contain characters that are not allowed in URLs, such as spaces. Here, they will be encoded.
            promQl = URLEncoder.encode(promQl, "utf-8");
            queryUrl = flinkDiagnosisConfig.getFlinkPrometheusHost() + MonitorMetricConstant.QUERY_RANGE_QUERY + promQl;
            return monitorMetricUtil.query(queryUrl, start, end, step);
        } catch (Throwable e) {
            log.error(e.getMessage() + queryUrl, e);
            return null;
        }
    }

    public List<MetricResult.DataResult> getJobMetrics(String promQl) {
        long start = System.currentTimeMillis() / 1000 - 60 * 2;
        long end = System.currentTimeMillis() / 1000;
        return getJobMetrics(promQl, start, end);
    }

    private Integer doubleToInteger(Double value) {
        if (value == null) {
            return null;
        } else {
            return value.intValue();
        }
    }

    private Float doubleToFloat(Double value) {
        if (value == null) {
            return null;
        } else {
            return value.floatValue();
        }
    }

    public String addLabel(String promql, String key, String value) {
//        String reg = ".*(\\{[^}]*}).*";
        String reg = "(\\{[^}]*})";
        Pattern compile = Pattern.compile(reg);
        Matcher matcher = compile.matcher(promql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String g = matcher.group(1);
            String[] split = g.substring(1, g.length() - 1).split(",");
            List<String> collect = Arrays.stream(split)
                    .filter(new Predicate<String>() {
                        @Override
                        public boolean test(String s) {
                            return s != null && !s.equals("");
                        }
                    })
                    .collect(Collectors.toList());
            collect.add(String.format("%s=~\"%s\"", key, value));
            String rep = "{" + StringUtils.join(collect, ",") + "}";
            matcher.appendReplacement(sb, rep);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Get metrics.
     *
     * @param start 秒
     * @param end   秒
     * @return
     */
    public DiagnosisContext getMetrics(DiagnosisContext context, long start, long end) {
        log.info("{} diagnosis: Fetching metrics for the time range start: {} end: {}.",
                context.getRcJobDiagnosis().getJobName(), start, end);
        // Calculate one hour after the start time of diagnosis.
        // important!The "uptime" metric uses job_id, while other metrics are obtained by getting the job
        // through the "uptime" metric. Therefore, "uptime" needs to be executed first.
        Long tsAfter1Hour = getTsAfter1Hour(context, start, end);
        if (tsAfter1Hour == null) {
            tsAfter1Hour = start + cons.getDiagnosisAfterMinutesMax() * 60;
        }
        String metricJob = context.getMessages().get(JobId).toString();
        // Set the start time to the earliest time duration that is covered by the latest slot number.

        try {
            String totalTmSlotCountsPromql = addLabel(TOTAL_TM_SLOT_COUNTS, "job", context.getMessages().get(Job).toString());
            List<MetricResult.DataResult> totalTmSlotCountsMetrics =
                    getJobMetrics(totalTmSlotCountsPromql, start, end);
            if (totalTmSlotCountsMetrics != null && totalTmSlotCountsMetrics.size() != 0) {
                Optional<MetricResult.DataResult> latestDr = totalTmSlotCountsMetrics.stream()
                        .max(new Comparator<MetricResult.DataResult>() {
                            @Override
                            public int compare(MetricResult.DataResult o1, MetricResult.DataResult o2) {
                                Optional<Integer> ts1 = monitorMetricUtil.getKeyValueStream(o1).get()
                                        .map(MetricResult.KeyValue::getTs).max(Integer::compare);
                                Optional<Integer> ts2 = monitorMetricUtil.getKeyValueStream(o2).get()
                                        .map(MetricResult.KeyValue::getTs).max(Integer::compare);
                                if (!ts1.isPresent()) {
                                    return -1;
                                }
                                if (!ts2.isPresent()) {
                                    return 1;
                                }
                                return Integer.compare(ts1.get(), ts2.get());
                            }
                        });
                Double latestTotalSlots = monitorMetricUtil.getLatestOrNull(Lists.newArrayList(latestDr.get()));
                if (latestTotalSlots != null) {
                    //  Set the actual parallelism.
                    context.getRcJobDiagnosis().setParallel((int) Math.floor(latestTotalSlots));
                    Integer latestElasticTs = monitorMetricUtil.getKeyValueStream(latestDr.get()).get()
                            .map(MetricResult.KeyValue::getTs).min(Integer::compare).get();
                    if (latestElasticTs != start) {
                        log.info(String.format("%s diagnosis selects the latest scaling start ts: %d.",
                                context.getRcJobDiagnosis().getJobName(), latestElasticTs));
                        start = latestElasticTs;
                    }
                }

            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        // Get the total number of TMs.
        List<MetricResult.DataResult> totalTmCountsMetrics = getJobMetrics(TOTAL_TM_COUNTS, context, start, end);
        if (totalTmCountsMetrics != null && totalTmCountsMetrics.size() == 1) {
            Double latestTotalTmNum = monitorMetricUtil.getLatestOrNull(totalTmCountsMetrics);
            // Set the total number of TMs.
            context.getRcJobDiagnosis().setTmNum((int) Math.floor(latestTotalTmNum));
            // Calculate the number of slots in a single TM.
            context.getRcJobDiagnosis().setTmSlotNum((int) Math.ceil((float) context.getRcJobDiagnosis()
                    .getParallel() / context.getRcJobDiagnosis().getTmNum()));
        }
        context.getMetrics().put(TOTAL_TM_COUNTS, totalTmCountsMetrics);

        // Calculate the start time point for diagnosis according to the latency.
        Long delayEndTs = getTsDelayEnd(context, start, end);
        if (delayEndTs == null) {
            delayEndTs = Long.MAX_VALUE;
        }

        // Calculate the time when the latency continues to rise for 5 minutes.
        Long tsAfter5MinOffsetGrow = getTsAfter5MinutesOffsetGrow(context, start, end);
        if (tsAfter5MinOffsetGrow == null) {
            tsAfter5MinOffsetGrow = Long.MAX_VALUE;
        }
        // Calculate if the delay catching up is relatively slow.
        Long tsStartIfDelayAndCatchUpSlow = getTsIfCatchUpDelaySlow(context, start, end);
        if (tsStartIfDelayAndCatchUpSlow == null) {
            tsStartIfDelayAndCatchUpSlow = Long.MAX_VALUE;
        }
        log.info("{} diagnosis end time point for latency: {}.", context.getRcJobDiagnosis().getJobName(), delayEndTs);
        log.info("{} diagnosis time point for {} minutes after the start of diagnosis: {}.", context.getRcJobDiagnosis().getJobName(),
                cons.getDiagnosisAfterMinutesMax(), tsAfter1Hour);
        log.info("{} diagnosis time point for the duration of the continually increasing offset: {}.", context.getRcJobDiagnosis().getJobName(),
                tsAfter5MinOffsetGrow);
        log.info("{} diagnosis start time point for the delay catching up slowly: {}.", context.getRcJobDiagnosis().getJobName(),
                tsStartIfDelayAndCatchUpSlow);

        start = Math.min(tsStartIfDelayAndCatchUpSlow, Math.min(tsAfter5MinOffsetGrow, Math.min(delayEndTs, tsAfter1Hour)));
        log.info("{} diagnosis selected time point: {}.", context.getRcJobDiagnosis().getJobName(), start);
        if (start >= end - cons.getElasticMinInterval() * 60) {
            log.info("{} diagnosis has not completed the delay catching up, has not been running for an hour, the offset" +
                            " has not been continuously increasing, or did not meet the scaling interval of {} minutes.",
                    context.getRcJobDiagnosis().getJobName(), cons.getElasticMinInterval());
            context.setStopResourceDiagnosis(true);
            return context;
        }
        // Get monitoring metrics and generate diagnostic metrics.
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        // Average CPU usage of the TM.
        List<MetricResult.DataResult> tmAvgCpuUsageRateMetrics =
                getTaskManagerMetrics(TM_AVG_CPU_USAGE_RATE, context, start, end);
        rcJobDiagnosis.setTmAvgCpuUsageMax(doubleToFloat(monitorMetricUtil.getMaxOrNull(tmAvgCpuUsageRateMetrics)));
        rcJobDiagnosis.setTmAvgCpuUsageMin(doubleToFloat(monitorMetricUtil.getMinOrNull(tmAvgCpuUsageRateMetrics)));
        rcJobDiagnosis.setTmAvgCpuUsageAvg(doubleToFloat(monitorMetricUtil.getAvgOrNull(tmAvgCpuUsageRateMetrics)));
        context.getMetrics().put(TM_AVG_CPU_USAGE_RATE, tmAvgCpuUsageRateMetrics);
        // CPU usage of a single TM.
        List<MetricResult.DataResult> tmCpuUsageRateMetrics = getTaskManagerMetrics(TM_CPU_USAGE_RATE, context, start, end);
        context.getMetrics().put(TM_CPU_USAGE_RATE, tmCpuUsageRateMetrics);
        // Traffic of a TM.
        List<MetricResult.DataResult> tmDataFlowRateMetrics = getTaskManagerMetrics(TM_DATA_FLOW_RATE, context, start, end);
        context.getMetrics().put(TM_DATA_FLOW_RATE, tmDataFlowRateMetrics);
        // Traffic of the job.
        List<MetricResult.DataResult> jobDataFlowMetrics = getTaskManagerMetrics(JOB_DATA_FLOW_RATE, context, start, end);
        context.getMetrics().put(JOB_DATA_FLOW_RATE, jobDataFlowMetrics);
        Integer maxFlow = doubleToInteger(monitorMetricUtil.getMaxOrNull(jobDataFlowMetrics));
        if (maxFlow != null) {
            context.getMessages().put(FlowMax, maxFlow);
        }
        // Calculate the traffic peaks and valleys throughout the day.
        List<MetricResult.DataResult> jobDataFlowMetricsHourly = getTaskManagerMetrics(JOB_DATA_FLOW_RATE, context, start, end, 60 * 60);
        context.getMetrics().put(JOB_DATA_FLOW_RATE_TROUGH, jobDataFlowMetricsHourly);
        // Slow operator(vertices) detection.
        List<MetricResult.DataResult> backPressureBlockSubtaskDetailMetrics = getTaskManagerMetrics(SLOW_VERTICES, context, start, end);
        if (backPressureBlockSubtaskDetailMetrics != null) {
            context.getMetrics().put(SLOW_VERTICES, backPressureBlockSubtaskDetailMetrics);
            log.debug("Back pressure blocking of operators.:" + backPressureBlockSubtaskDetailMetrics);
        }
        // Managed memory used by the TM.
        List<MetricResult.DataResult> tmManageMemUsageMetrics = getTaskManagerMetrics(TM_MANAGE_MEM_USAGE, context, start, end);
        context.getMetrics().put(TM_MANAGE_MEM_USAGE, tmManageMemUsageMetrics);

        // kafka partition
        List<MetricResult.DataResult> kafkaPartitionsMetrics = getTaskManagerMetrics(KAFKA_PARTITIONS, context, start, end);
        context.getMetrics().put(KAFKA_PARTITIONS, kafkaPartitionsMetrics);
        // Get the number of Kafka partitions recently.
        Double maxPartition = monitorMetricUtil.getLatestOrNull(kafkaPartitionsMetrics);
        context.getRcJobDiagnosis().setKafkaConsumePartitionNum(maxPartition == null ? null : maxPartition.intValue());

        // Heap memory used by the TM.
        List<MetricResult.DataResult> tmUsageHeapMemMaxMetrics = getTaskManagerMetrics(TM_USAGE_HEAP_MEM_MAX, context, start, end);
        context.getMetrics().put(TM_USAGE_HEAP_MEM_MAX, tmUsageHeapMemMaxMetrics);
        // Total heap memory of the TM.
        List<MetricResult.DataResult> tmTotalHeapMemMaxMetrics = getTaskManagerMetrics(TM_TOTAL_HEAP_MEM_MAX, context, start, end);
        context.getMetrics().put(TM_TOTAL_HEAP_MEM_MAX, tmTotalHeapMemMaxMetrics);
        // Total managed memory of the TM.
        List<MetricResult.DataResult> tmManageMemTotalMetrics = getTaskManagerMetrics(TM_MANAGE_MEM_TOTAL, context, start, end);
        context.getMetrics().put(TM_MANAGE_MEM_TOTAL, tmManageMemTotalMetrics);

        // Back pressure operators(vertices).
        List<MetricResult.DataResult> backPressureVerticesMetrics = getTaskManagerMetrics(BACK_PRESSURE_VERTICES, context, start, end);
        context.getMetrics().put(BACK_PRESSURE_VERTICES, backPressureVerticesMetrics);

        // Heap memory usage rate of a single TM.
        List<MetricResult.DataResult> tmHeapMemUsageRateMetrics = getTaskManagerMetrics(TM_HEAP_MEM_USAGE_RATE, context, start, end);
        context.getMetrics().put(TM_HEAP_MEM_USAGE_RATE, tmHeapMemUsageRateMetrics);

        return context;
    }

    private Long getTsDelayEnd(DiagnosisContext context, long start, long end) {
        List<MetricResult.DataResult> lagRecordsNumMetrics = getTaskManagerMetrics(SUM_RECORDS_LAG_PROMQL, context, start, end);
        if (lagRecordsNumMetrics == null || lagRecordsNumMetrics.size() != 1) {
            return null;
        }
        Optional<Integer> min = monitorMetricUtil.getSmoothKeyValueStream(lagRecordsNumMetrics.get(0), 5)
                .get()
                .filter(kv -> (kv.getValue() != null && kv.getValue() < cons.getDiagnosisMinDelayAfterRunning()))
                .map(MetricResult.KeyValue::getTs)
                .min(Integer::compareTo);
        if (min.isPresent()) {
            return min.get().longValue();
        } else {
            return null;
        }
    }

    private Double getLatestMaxUpTime(DiagnosisContext context, long start, long end) {
        List<MetricResult.DataResult> jobUpTimeMetrics = getJobManagerMetrics(JOB_UP_TIME, context, start, end);
        if (jobUpTimeMetrics != null) {
            MetricResult.DataResult latestResult = jobUpTimeMetrics.stream().max((o1, o2) -> {
                Supplier<Stream<List<Object>>> s1 = monitorMetricUtil.getTupleStream(o1);
                Supplier<Stream<List<Object>>> s2 = monitorMetricUtil.getTupleStream(o2);
                Integer maxTs1 = s1.get().map(x -> (Integer) x.get(0)).max(Integer::compareTo).orElse(Integer.MIN_VALUE);
                Integer maxTs2 = s2.get().map(x -> (Integer) x.get(0)).max(Integer::compareTo).orElse(Integer.MIN_VALUE);
                return Integer.compare(maxTs1, maxTs2);
            }).orElse(null);
            if (latestResult == null) {
                log.error(String.format("%s the latest online time is empty.", context.getRcJobDiagnosis().getJobName()));
                return null;
            }
            return monitorMetricUtil.getLatestOrNull(Lists.newArrayList(latestResult));
        }
        return null;
    }

    /**
     * Only the uptime metric uses the job_id, while all other metrics are obtained through the metric of uptime to obtain the job
     *
     * @param context
     * @param start
     * @param end
     * @return Timestamp (In seconds)
     */
    private Long getTsAfter1Hour(DiagnosisContext context, long start, long end) {
        List<MetricResult.DataResult> jobUpTimeMetrics = getJobManagerMetrics(JOB_UP_TIME, context, start, end);
        if (jobUpTimeMetrics != null) {
            int diagnosisStartUpTimeMilliseconds = cons.getDiagnosisAfterMinutesMax() * 60 * 1000;
            MetricResult.DataResult latestResult = jobUpTimeMetrics.stream().max((o1, o2) -> {
                Supplier<Stream<List<Object>>> s1 = monitorMetricUtil.getTupleStream(o1);
                Supplier<Stream<List<Object>>> s2 = monitorMetricUtil.getTupleStream(o2);
                Integer maxTs1 = s1.get().map(x -> (Integer) x.get(0)).max(Integer::compareTo).orElse(Integer.MIN_VALUE);
                Integer maxTs2 = s2.get().map(x -> (Integer) x.get(0)).max(Integer::compareTo).orElse(Integer.MIN_VALUE);
                return Integer.compare(maxTs1, maxTs2);
            }).orElse(null);
            if (latestResult == null) {
                log.error(String.format("%s The latest online time is empty.", context.getRcJobDiagnosis().getJobName()));
                return null;
            }
            String job = latestResult.getMetric().get("job").toString();
            context.getMessages().put(Job, job);
            List<List<Object>> resultList = monitorMetricUtil.getTupleStream(latestResult).get().sorted((o1, o2) -> {
                Integer ts1 = (Integer) o1.get(0);
                Integer ts2 = (Integer) o2.get(0);
                return Integer.compare(ts1, ts2);
            }).collect(Collectors.toList());
            if (resultList.size() == 0) {
                log.error(String.format("%s The latest online time metric has 0 data points.", context.getRcJobDiagnosis().getJobName()));
                return null;
            }
            Integer minTs = (Integer) resultList.get(0).get(0);
            for (int i = 0; i < resultList.size(); i++) {
                List<Object> data = resultList.get(i);
                Integer ts = (Integer) data.get(0);
                double milliseconds = Double.parseDouble((String) data.get(1));
                if (milliseconds > diagnosisStartUpTimeMilliseconds || ts - minTs > diagnosisStartUpTimeMilliseconds / 1000) {
                    log.info(String.format("%s the diagnostic task started one hour ago at ts:%d.", context.getRcJobDiagnosis().getJobName(), ts));
                    return ts.longValue();
                } else {
                    return (long) ts + (long) (diagnosisStartUpTimeMilliseconds - milliseconds) / 1000;
                }
//                if (i == resultList.size() - 1) {
//                    log.info(String.format("%s任务上线没有到1小时", context.getRcJobDiagnosis().getJobName()));
//                    return (long) ts + (long) (diagnosisStartUpTimeMilliseconds - milliseconds) / 1000;
//                }
            }
        }
        return null;
    }


    /**
     * Calculate the time point that rises 15 minutes after the delay.
     *
     * @param context
     * @param start
     * @param end
     * @return
     */
    private Long getTsAfter5MinutesOffsetGrow(DiagnosisContext context, long start, long end) {
        List<MetricResult.DataResult> offsetDeltaMetrics = getTaskManagerMetrics(OFFSET_DELTA, context, start, end);
        if (offsetDeltaMetrics != null) {
            if (offsetDeltaMetrics.size() != 1) {
                log.info("The number of {} job offset delta list is not 1.", context.getRcJobDiagnosis().getJobName());
                return null;
            }
            // Retrieve the point at which the onset of delay increase occurs
            Supplier<Stream<MetricResult.KeyValue>> kvSmooth = monitorMetricUtil
                    .getWindowReduceKeyValueStream(monitorMetricUtil.getKeyValueStream(
                            offsetDeltaMetrics.get(0)), 5, new Function<Stream<Double>, OptionalDouble>() {
                        @Override
                        public OptionalDouble apply(Stream<Double> doubleStream) {
                            return doubleStream.filter(Objects::nonNull).mapToDouble(value -> value).average();
                        }
                    });
            Optional<MetricResult.KeyValue> firstDelay5Minute = kvSmooth.get()
                    .filter(x -> (x.getValue() != null && x.getValue() > 0)).findFirst();
            if (firstDelay5Minute.isPresent()) {
                return (long) firstDelay5Minute.get().getTs();
            } else {
                log.info("{} Delay did not rise continuously for 5 minutes.", context.getRcJobDiagnosis().getJobName());
                return null;
            }
        } else {
            log.info("{} job offset delta 为null", context.getRcJobDiagnosis().getJobName());
            return null;
        }
    }

    /**
     * To determine if catching up with the delay is slower than expected, and whether additional resources are needed, you can follow these steps
     *
     * @param context
     * @param start   Second
     * @param end     Second
     * @return Second
     */
    private Long getTsIfCatchUpDelaySlow(DiagnosisContext context, long start, long end) {
        try {
            // 首先判断是不是还有延迟
            List<MetricResult.DataResult> maxTimeLagPromqlMetrics = getTaskManagerMetrics(MAX_TIME_LAG_PROMQL, context, start, end);
            if (maxTimeLagPromqlMetrics == null || maxTimeLagPromqlMetrics.size() != 1) {
                log.info("{} The delay metric is empty, so it cannot be determined if catching up with the delay is too slow", context.getRcJobDiagnosis().getJobName());
                return null;
            }
            context.getMetrics().put(MAX_TIME_LAG_PROMQL, maxTimeLagPromqlMetrics);
            Double delaySec = monitorMetricUtil.getLatestOrNull(maxTimeLagPromqlMetrics);
            if (delaySec == null || delaySec < cons.getCatchUpDelayThreshold()) {
                log.info("{} Delay is {} or within {} seconds.", context.getRcJobDiagnosis().getJobName(), delaySec, cons.getCatchUpDelayThreshold());
                return null;
            }
            // Then, check if the ratio of consumption rate to production rate has reached the threshold value.
            List<MetricResult.DataResult> consumeDivideProduceRateMetrics = getTaskManagerMetrics(CONSUME_DIVIDE_PRODUCE_RATE, context, start, end);
            if (consumeDivideProduceRateMetrics == null || consumeDivideProduceRateMetrics.size() != 1) {
                log.info("{} The consumer-to-producer rate ratio metric is empty.", context.getRcJobDiagnosis().getJobName());
                return null;
            }
            Double latestConsumeDivideProduceRate = monitorMetricUtil.getLatestOrNull(consumeDivideProduceRateMetrics);
            if (latestConsumeDivideProduceRate == null || latestConsumeDivideProduceRate > cons.getCatchUpConsumeDivideProduceThreshold()) {
                log.info("{} The consumer-to-producer rate ratio is: {} > {}.", context.getRcJobDiagnosis().getJobName(),
                        latestConsumeDivideProduceRate, cons.getCatchUpConsumeDivideProduceThreshold());
                return null;
            }
            log.info("{} The delay exists: {} seconds, which is higher than {} seconds. The consumer-to-producer rate ratio is {} below {}..",
                    context.getRcJobDiagnosis().getJobName(), delaySec, cons.getCatchUpDelayThreshold(),
                    latestConsumeDivideProduceRate, cons.getCatchUpConsumeDivideProduceThreshold());
            return start;
        } catch (Throwable t) {
            log.error("Error occurred while calculating whether the job was catching up the delay slowly for {} job.", context.getRcJobDiagnosis().getJobName());
            log.error(t.getMessage(), t);
            return null;
        }
    }
}
