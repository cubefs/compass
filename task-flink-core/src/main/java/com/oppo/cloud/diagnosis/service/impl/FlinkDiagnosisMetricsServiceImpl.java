package com.oppo.cloud.diagnosis.service.impl;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.config.FlinkDiagnosisConfig;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.diagnosis.constant.MonitorMetricConstant;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.util.DoctorUtil;
import com.oppo.cloud.diagnosis.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import static com.oppo.cloud.diagnosis.constant.MonitorMetricConstant.*;


/**
 * 诊断指标获取模块
 */
@Slf4j
@Service
public class FlinkDiagnosisMetricsServiceImpl {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    DoctorUtil doctorUtil;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Autowired
    FlinkDiagnosisConfig flinkDiagnosisConfig;

    public double getJobMetricCurrentValueMax(String promQl) {
        try {
            List<MetricResult.DataResult> dataResults = getJobMetrics(promQl);
            return monitorMetricUtil.getLatest(dataResults);
        } catch (Exception e) {
            log.error("执行promQL失败");
            log.error(e.getMessage() + promQl, e);
        }
        return 0D;
    }

    public double getJobMetricCurrentValueMin(String promQl) {
        try {
            List<MetricResult.DataResult> dataResults = getJobMetrics(promQl);
            return monitorMetricUtil.getMin(dataResults);
        } catch (Exception e) {
            log.error("执行promQL失败");
            log.error(e.getMessage() + promQl, e);
        }
        return 0D;
    }

    public double getJobMetricCurrentValueAvg(String promQl) {
        try {
            List<MetricResult.DataResult> dataResults = getJobMetrics(promQl);
            return monitorMetricUtil.getAvg(dataResults);
        } catch (Exception e) {
            log.error("执行promQL失败");
            log.error(e.getMessage() + promQl, e);
        }
        return 0D;
    }

    /**
     * 查询metrics 数据
     *
     * @param promQl promql查询语句
     * @param start  起始时间戳 秒级别
     * @param end    结束时间戳 秒级别
     * @return 查询结果
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
     * 查询 metrics 数据
     *
     * @param promQl pronql查询语句
     * @param start  起始时间戳 秒级别
     * @param end    结束时间戳 秒级别
     * @param step   步长 秒级别
     * @return 查询结果
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
            // promQl 可能包含url不允许的字符如空格，在这里进行encode
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
     * 获取metrics
     *
     * @param start 秒
     * @param end   秒
     * @return
     */
    public DiagnosisContext getMetrics(DiagnosisContext context, long start, long end) {
        log.info("{} 诊断获取指标,时间范围 start:{} end:{}", context.getRcJobDiagnosis().getJobName(), start, end);
        // 计算诊断开始后一小时的时间点
        // important!只有uptime指标用的是job_id，其余的指标都通过uptime 的metric 获取的job来获取，所以要先执行uptime
        Long tsAfter1Hour = getTsAfter1Hour(context, start, end);
        if (tsAfter1Hour == null) {
            tsAfter1Hour = start + cons.getDiagnosisAfterMinutesMax() * 60;
        }
        String metricJob = context.getMessages().get(JobId).toString();
        // 把start设置为最新的slot数持续的时间最早的时间

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
                    //  设置实际的并行度
                    context.getRcJobDiagnosis().setParallel((int) Math.floor(latestTotalSlots));
                    Integer latestElasticTs = monitorMetricUtil.getKeyValueStream(latestDr.get()).get()
                            .map(MetricResult.KeyValue::getTs).min(Integer::compare).get();
                    if (latestElasticTs != start) {
                        log.info(String.format("%s 诊断选择了最近一次伸缩开始ts:%d", context.getRcJobDiagnosis().getJobName(), latestElasticTs));
                        start = latestElasticTs;
                    }
                }

            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        // 获取到总的tm个数
        List<MetricResult.DataResult> totalTmCountsMetrics = getJobMetrics(TOTAL_TM_COUNTS, context, start, end);
        if (totalTmCountsMetrics != null && totalTmCountsMetrics.size() == 1) {
            Double latestTotalTmNum = monitorMetricUtil.getLatestOrNull(totalTmCountsMetrics);
            // 设置总的tm个数
            context.getRcJobDiagnosis().setTmNum((int) Math.floor(latestTotalTmNum));
            // 计算单个tm的slot数量
            context.getRcJobDiagnosis().setTmSlotNum((int) Math.ceil((float) context.getRcJobDiagnosis()
                    .getParallel() / context.getRcJobDiagnosis().getTmNum()));
        }
        context.getMetrics().put(TOTAL_TM_COUNTS, totalTmCountsMetrics);

        // 根据延迟计算诊断起始时间点
        Long delayEndTs = getTsDelayEnd(context, start, end);
        if (delayEndTs == null) {
            delayEndTs = Long.MAX_VALUE;
        }

        // 计算延迟持续5分钟上涨的时间
        Long tsAfter5MinOffsetGrow = getTsAfter5MinutesOffsetGrow(context, start, end);
        if (tsAfter5MinOffsetGrow == null) {
            tsAfter5MinOffsetGrow = Long.MAX_VALUE;
        }
        // 计算是否追延迟比较慢
        Long tsStartIfDelayAndCatchUpSlow = getTsIfCatchUpDelaySlow(context, start, end);
        if (tsStartIfDelayAndCatchUpSlow == null) {
            tsStartIfDelayAndCatchUpSlow = Long.MAX_VALUE;
        }
        log.info("{} 诊断延迟结束的时间点: {}", context.getRcJobDiagnosis().getJobName(), delayEndTs);
        log.info("{} 诊断running后{}分钟的时间点: {}", context.getRcJobDiagnosis().getJobName(), cons.getDiagnosisAfterMinutesMax(), tsAfter1Hour);
        log.info("{} 诊断offset持续上涨的时间点: {}", context.getRcJobDiagnosis().getJobName(), tsAfter5MinOffsetGrow);
        log.info("{} 诊断追延迟慢的start: {}", context.getRcJobDiagnosis().getJobName(), tsStartIfDelayAndCatchUpSlow);
        start = Math.min(tsStartIfDelayAndCatchUpSlow, Math.min(tsAfter5MinOffsetGrow, Math.min(delayEndTs, tsAfter1Hour)));
        log.info("{} 诊断选择时间点: {}", context.getRcJobDiagnosis().getJobName(), start);
        if (start >= end - cons.getElasticMinInterval() * 60) {
            log.info("{} 诊断追延迟没有结束，没有运行达到1小时,没有offset持续上涨,或者没有达到伸缩间隔{}分钟",
                    context.getRcJobDiagnosis().getJobName(), cons.getElasticMinInterval());
            context.setStopResourceDiagnosis(true);
            return context;
        }
        // 获取监控指标，生成诊断指标
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        // tm 平均cpu使用率
        List<MetricResult.DataResult> tmAvgCpuUsageRateMetrics =
                getTaskManagerMetrics(TM_AVG_CPU_USAGE_RATE, context, start, end);
        rcJobDiagnosis.setTmAvgCpuUsageMax(doubleToFloat(monitorMetricUtil.getMaxOrNull(tmAvgCpuUsageRateMetrics)));
        rcJobDiagnosis.setTmAvgCpuUsageMin(doubleToFloat(monitorMetricUtil.getMinOrNull(tmAvgCpuUsageRateMetrics)));
        rcJobDiagnosis.setTmAvgCpuUsageAvg(doubleToFloat(monitorMetricUtil.getAvgOrNull(tmAvgCpuUsageRateMetrics)));
        context.getMetrics().put(TM_AVG_CPU_USAGE_RATE, tmAvgCpuUsageRateMetrics);
        // tm 单个cpu使用率
        List<MetricResult.DataResult> tmCpuUsageRateMetrics = getTaskManagerMetrics(TM_CPU_USAGE_RATE, context, start, end);
        context.getMetrics().put(TM_CPU_USAGE_RATE, tmCpuUsageRateMetrics);
        // tm 流量
        List<MetricResult.DataResult> tmDataFlowRateMetrics = getTaskManagerMetrics(TM_DATA_FLOW_RATE, context, start, end);
        context.getMetrics().put(TM_DATA_FLOW_RATE, tmDataFlowRateMetrics);
        // 作业流量
        List<MetricResult.DataResult> jobDataFlowMetrics = getTaskManagerMetrics(JOB_DATA_FLOW_RATE, context, start, end);
        context.getMetrics().put(JOB_DATA_FLOW_RATE, jobDataFlowMetrics);
        Integer maxFlow = doubleToInteger(monitorMetricUtil.getMaxOrNull(jobDataFlowMetrics));
        if (maxFlow != null) {
            context.getMessages().put(FlowMax, maxFlow);
        }
        // 计算一天当中的流量波峰波谷
        List<MetricResult.DataResult> jobDataFlowMetricsHourly = getTaskManagerMetrics(JOB_DATA_FLOW_RATE, context, start, end, 60 * 60);
        context.getMetrics().put(JOB_DATA_FLOW_RATE_TROUGH, jobDataFlowMetricsHourly);
        // 慢算子检测
        List<MetricResult.DataResult> backPressureBlockSubtaskDetailMetrics = getTaskManagerMetrics(SLOW_VERTICES, context, start, end);
        if (backPressureBlockSubtaskDetailMetrics != null) {
            context.getMetrics().put(SLOW_VERTICES, backPressureBlockSubtaskDetailMetrics);
            log.debug("反压算子阻塞:" + backPressureBlockSubtaskDetailMetrics);
        }
        // TM 使用的管理内存
        List<MetricResult.DataResult> tmManageMemUsageMetrics = getTaskManagerMetrics(TM_MANAGE_MEM_USAGE, context, start, end);
        context.getMetrics().put(TM_MANAGE_MEM_USAGE, tmManageMemUsageMetrics);

        // kafka partition
        List<MetricResult.DataResult> kafkaPartitionsMetrics = getTaskManagerMetrics(KAFKA_PARTITIONS, context, start, end);
        context.getMetrics().put(KAFKA_PARTITIONS, kafkaPartitionsMetrics);
        // 获取最近的kafka 分区数
        Double maxPartition = monitorMetricUtil.getLatestOrNull(kafkaPartitionsMetrics);
        context.getRcJobDiagnosis().setKafkaConsumePartitionNum(maxPartition == null ? null : maxPartition.intValue());

        // tm使用堆内存
        List<MetricResult.DataResult> tmUsageHeapMemMaxMetrics = getTaskManagerMetrics(TM_USAGE_HEAP_MEM_MAX, context, start, end);
        context.getMetrics().put(TM_USAGE_HEAP_MEM_MAX, tmUsageHeapMemMaxMetrics);
        // tm总的堆内存
        List<MetricResult.DataResult> tmTotalHeapMemMaxMetrics = getTaskManagerMetrics(TM_TOTAL_HEAP_MEM_MAX, context, start, end);
        context.getMetrics().put(TM_TOTAL_HEAP_MEM_MAX, tmTotalHeapMemMaxMetrics);
        // TM manage 内存总量
        List<MetricResult.DataResult> tmManageMemTotalMetrics = getTaskManagerMetrics(TM_MANAGE_MEM_TOTAL, context, start, end);
        context.getMetrics().put(TM_MANAGE_MEM_TOTAL, tmManageMemTotalMetrics);

        // 反压算子
        List<MetricResult.DataResult> backPressureVerticesMetrics = getTaskManagerMetrics(BACK_PRESSURE_VERTICES, context, start, end);
        context.getMetrics().put(BACK_PRESSURE_VERTICES, backPressureVerticesMetrics);

        // 单个tm堆内存使用率
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
                log.error(String.format("%s 最近一次上线时间为空", context.getRcJobDiagnosis().getJobName()));
                return null;
            }
            return monitorMetricUtil.getLatestOrNull(Lists.newArrayList(latestResult));
        }
        return null;
    }

    /**
     * 只有uptime指标用的是job_id，其余的指标都通过uptime 的metric 获取的job来获取
     *
     * @param context
     * @param start
     * @param end
     * @return 秒级别时间戳
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
                log.error(String.format("%s 最近一次上线时间为空", context.getRcJobDiagnosis().getJobName()));
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
                log.error(String.format("%s 最近一次上线时间指标点数为0", context.getRcJobDiagnosis().getJobName()));
                return null;
            }
            Integer minTs = (Integer) resultList.get(0).get(0);
            for (int i = 0; i < resultList.size(); i++) {
                List<Object> data = resultList.get(i);
                Integer ts = (Integer) data.get(0);
                double milliseconds = Double.parseDouble((String) data.get(1));
                if (milliseconds > diagnosisStartUpTimeMilliseconds || ts - minTs > diagnosisStartUpTimeMilliseconds / 1000) {
                    log.info(String.format("%s 诊断任务开始一小时时间为ts:%d", context.getRcJobDiagnosis().getJobName(), ts));
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
     * 计算延迟15分钟上涨的时间点
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
                log.info("{} job offset delta 列表数量不为1", context.getRcJobDiagnosis().getJobName());
                return null;
            }
            // 获取延迟开始上涨的那个点
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
                log.info("{} 延迟没有连续5分钟上涨", context.getRcJobDiagnosis().getJobName());
                return null;
            }
        } else {
            log.info("{} job offset delta 为null", context.getRcJobDiagnosis().getJobName());
            return null;
        }
    }

    /**
     * 计算是否追延迟比较慢,如果比较慢，则需要扩容资源
     *
     * @param context
     * @param start   秒
     * @param end     秒
     * @return 秒
     */
    private Long getTsIfCatchUpDelaySlow(DiagnosisContext context, long start, long end) {
        try {
            // 首先判断是不是还有延迟
            List<MetricResult.DataResult> maxTimeLagPromqlMetrics = getTaskManagerMetrics(MAX_TIME_LAG_PROMQL, context, start, end);
            if (maxTimeLagPromqlMetrics == null || maxTimeLagPromqlMetrics.size() != 1) {
                log.info("{} 延迟指标为空，不能判断是否追延迟太慢", context.getRcJobDiagnosis().getJobName());
                return null;
            }
            context.getMetrics().put(MAX_TIME_LAG_PROMQL, maxTimeLagPromqlMetrics);
            Double delaySec = monitorMetricUtil.getLatestOrNull(maxTimeLagPromqlMetrics);
            if (delaySec == null || delaySec < cons.getCatchUpDelayThreshold()) {
                log.info("{} 延迟为 {} 或在{} second 内", context.getRcJobDiagnosis().getJobName(), delaySec, cons.getCatchUpDelayThreshold());
                return null;
            }
            // 然后判断消费速率与生产速率的比值有没有达到阈值
            List<MetricResult.DataResult> consumeDivideProduceRateMetrics = getTaskManagerMetrics(CONSUME_DIVIDE_PRODUCE_RATE, context, start, end);
            if (consumeDivideProduceRateMetrics == null || consumeDivideProduceRateMetrics.size() != 1) {
                log.info("{} 消费生产速率比值指标为空", context.getRcJobDiagnosis().getJobName());
                return null;
            }
            Double latestConsumeDivideProduceRate = monitorMetricUtil.getLatestOrNull(consumeDivideProduceRateMetrics);
            if (latestConsumeDivideProduceRate == null || latestConsumeDivideProduceRate > cons.getCatchUpConsumeDivideProduceThreshold()) {
                log.info("{} 消费生产速率比值:{} > {}", context.getRcJobDiagnosis().getJobName(),
                        latestConsumeDivideProduceRate, cons.getCatchUpConsumeDivideProduceThreshold());
                return null;
            }
            log.info("{} 存在延迟:{} second 高于 {} second,消费生产速率比值:{} 未达到 {}",
                    context.getRcJobDiagnosis().getJobName(), delaySec, cons.getCatchUpDelayThreshold(),
                    latestConsumeDivideProduceRate, cons.getCatchUpConsumeDivideProduceThreshold());
            return start;
        } catch (Throwable t) {
            log.error("{} 作业计算追延迟是否慢报错", context.getRcJobDiagnosis().getJobName());
            log.error(t.getMessage(), t);
            return null;
        }
    }
}
