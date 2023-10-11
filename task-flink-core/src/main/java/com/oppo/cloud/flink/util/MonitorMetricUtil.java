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

package com.oppo.cloud.flink.util;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Monitoring Tool
 */
@Getter
@Slf4j
@Component
public class MonitorMetricUtil {
    private final HttpUtil httpUtil = new HttpUtil();

    /**
     * Query promql
     *
     * @param url
     * @param start
     * @param end
     * @param step
     * @return
     */
    public List<MetricResult.DataResult> query(String url, long start, long end, long step) {
        String queryUrl = url + "&start=" + start + "&end=" + end + "&step=" + step;
        return doHttpQuery(queryUrl);
    }

    public List<MetricResult.DataResult> query(String url, long start, long end) {
        long step = getStep(start, end);
        return query(url, start, end, step);
    }

    public List<MetricResult.DataResult> doHttpQuery(String queryUrl) {
        try {
            log.debug("doHttpQuery queryUrl: {}", queryUrl);
            String responseStr = httpUtil.get(queryUrl);
            if (responseStr == null) {
                return null;
            }
            if (responseStr.contains("value\":[")) {
                // 有的数据是以单个元素value返回的,此处做特殊处理兼容
                responseStr = responseStr.replaceAll("value\":\\[", "values\":[");
                responseStr = responseStr.replaceAll("]}]}}", "]]}]}}");
            }
            log.debug("doHttpQuery responseStr: {}", responseStr);

            MetricResult metricResult = JSON.parseObject(responseStr, MetricResult.class);
            return metricResult.getData().getResult();
        } catch (Throwable e) {
            log.error(e.getMessage() + queryUrl);
            return null;
        }

    }

    public List<String> getTag(List<MetricResult.DataResult> result, String tag) {
        if (!CollectionUtils.isEmpty(result)) {
            return result.stream().map(x -> {
                Map<String, Object> metric = x.getMetric();
                return (String) metric.get(tag);
            }).collect(Collectors.toList());
        }

        return Lists.newArrayList();
    }

    public Double getMaxOrNull(MetricResult.DataResult result) {
        if (result == null) {
            return null;
        }
        return getMaxOrNull(Collections.singletonList(result));
    }

    public Double getMaxOrNull(List<MetricResult.DataResult> result) {
        if (result == null || result.size() == 0) {
            return null;
        } else {
            return getMax(result);
        }
    }

    public Double getMaxOrNegative(List<MetricResult.DataResult> result) {
        Double maxResult = getMaxOrNull(result);
        return Objects.isNull(maxResult) ? -1.0 : maxResult;
    }

    public Double getLatestOrNull(List<MetricResult.DataResult> result) {
        if (result == null || result.size() == 0) {
            return null;
        } else {
            return getLatest(result);
        }
    }

    public Double getMinOrNull(List<MetricResult.DataResult> result) {
        if (result == null || result.size() == 0) {
            return null;
        } else {
            return getMin(result);
        }
    }

    public Double getAvgOrNull(List<MetricResult.DataResult> result) {
        if (result == null || result.size() == 0) {
            return null;
        } else {
            return getAvg(result);
        }
    }

    public Supplier<Stream<Double>> getValueStream(List<MetricResult.DataResult> result) {
        if (CollectionUtils.isEmpty(result)) {
            return null;
        }
        return getValueStream(result.get(0));
    }

    public Supplier<Stream<Double>> getValueStream(MetricResult.DataResult result) {
        if (result == null) {
            return null;
        }
        List<List> values = (List<List>) result.getValues();
        return () -> values.stream().map(list -> list.get(1)).map(node -> {
                    try {
                        Double v = Double.valueOf((String) node);
                        if (v.isInfinite() || v.isNaN()) {
                            return null;
                        } else {
                            return v;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
        );
    }

    public Supplier<Stream<List<Object>>> getTupleStream(MetricResult.DataResult result) {
        if (result == null) {
            return null;
        }
        List<List> values = (List<List>) result.getValues();
        return () -> values.stream().map(x -> (List<Object>) x);
    }

    public Supplier<Stream<MetricResult.KeyValue>> getSmoothKeyValueStream(
            MetricResult.DataResult result, int smoothWindow) {
        return getSmoothKeyValueStream(getKeyValueStream(result), smoothWindow);
    }

    /**
     * Flatten curves.
     *
     * @param result
     * @return
     */
    public Supplier<Stream<MetricResult.KeyValue>> getFlatKeyValueStream(
            MetricResult.DataResult result) {
        return getFlatKeyValueStream(getKeyValueStream(result));
    }

    // Flatten curves without reducing the number of points.
    public Supplier<Stream<MetricResult.KeyValue>> getFlatKeyValueStream(Supplier<Stream<MetricResult.KeyValue>> stream) {
        if (stream == null) {
            return null;
        }
        return () -> {
            List<MetricResult.KeyValue> collect = stream.get()
                    .collect(Collectors.toList());
            List<MetricResult.KeyValue> result = new ArrayList<>();
            for (int i = 0; i < collect.size(); i++) {
                Double cur;
                cur = collect.get(i).getValue();
                Double left = cur;
                Double right = cur;
                if (i > 0) {
                    left = collect.get(i - 1).getValue();
                }
                if (i < collect.size() - 1) {
                    right = collect.get(i + 1).getValue();
                }
                if (cur != null && left != null && right != null && cur >= left && cur >= right) {
                    cur = Math.max(left, right);
                }
                result.add(new MetricResult.KeyValue(collect.get(i).getTs(), cur));
            }
            return result.stream();
        };
    }

    /**
     * Get the maximum value after smoothing.
     *
     * @param result
     * @param smoothWindow
     * @return
     */
    public Double getSmoothMaxOrNull(
            MetricResult.DataResult result, int smoothWindow) {
        OptionalDouble max = getSmoothKeyValueStream(getKeyValueStream(result), smoothWindow)
                .get().filter(kv -> kv.getValue() != null)
                .mapToDouble(MetricResult.KeyValue::getValue)
                .max();
        if (max.isPresent()) {
            return max.getAsDouble();
        } else {
            return null;
        }
    }

    public Double getMaxOrNull(Supplier<Stream<MetricResult.KeyValue>> stream
    ) {
        OptionalDouble max = stream.get().filter(kv -> kv.getValue() != null)
                .mapToDouble(MetricResult.KeyValue::getValue)
                .max();
        if (max.isPresent()) {
            return max.getAsDouble();
        } else {
            return null;
        }
    }

    /**
     * Get the minimum value after smoothing.
     *
     * @param result
     * @param smoothWindow
     * @return
     */
    public Double getSmoothMinOrNull(
            MetricResult.DataResult result, int smoothWindow) {
        OptionalDouble min = getSmoothKeyValueStream(getKeyValueStream(result), smoothWindow)
                .get().filter(kv -> kv.getValue() != null)
                .mapToDouble(MetricResult.KeyValue::getValue)
                .min();
        if (min.isPresent()) {
            return min.getAsDouble();
        } else {
            return null;
        }
    }

    public Double getSmoothAvgOrNull(
            MetricResult.DataResult result, int smoothWindow) {
        OptionalDouble avg = getSmoothKeyValueStream(getKeyValueStream(result), smoothWindow)
                .get().filter(kv -> kv.getValue() != null)
                .mapToDouble(MetricResult.KeyValue::getValue)
                .average();
        if (avg.isPresent()) {
            return avg.getAsDouble();
        } else {
            return null;
        }
    }

    /**
     * Smooth the curve and take the average of the points within the window.
     *
     * @param stream
     * @param smoothWindow
     * @return
     */
    public Supplier<Stream<MetricResult.KeyValue>> getSmoothKeyValueStream(
            Supplier<Stream<MetricResult.KeyValue>> stream, int smoothWindow) {
        if (stream == null) {
            return null;
        }
        return () -> {
            if (smoothWindow < 2) {
                throw new RuntimeException("The smoothing window size should be at least 2.");
            }
            List<MetricResult.KeyValue> collect = stream.get()
                    .collect(Collectors.toList());
            List<MetricResult.KeyValue> result = new ArrayList<>();
            for (int i = 0; i < collect.size() - (smoothWindow - 1); i++) {
                List<Double> smoothValues = new ArrayList<>();
                for (int j = 0; j < smoothWindow; j++) {
                    if (collect.get(i + j).getValue() != null) {
                        smoothValues.add(collect.get(i + j).getValue());
                    }
                }
                OptionalDouble smoothValueOption = smoothValues.stream().filter(Objects::nonNull)
                        .mapToDouble(value -> value)
                        .average();
                Double smoothValue = smoothValueOption.isPresent() ? smoothValueOption.getAsDouble() : null;
                result.add(new MetricResult.KeyValue(collect.get(i).getTs(), smoothValue));
            }
            return result.stream();
        };
    }

    /**
     * Calculate the aggregate value using the fn function on the values within the window.
     *
     * @param stream
     * @param smoothWindow
     * @param fn
     * @return
     */
    public Supplier<Stream<MetricResult.KeyValue>> getWindowReduceKeyValueStream(
            Supplier<Stream<MetricResult.KeyValue>> stream, int smoothWindow, Function<Stream<Double>, OptionalDouble> fn) {
        if (stream == null) {
            return null;
        }
        return () -> {
            if (smoothWindow < 2) {
                throw new RuntimeException("The aggregation window size should be at least 2.");
            }
            List<MetricResult.KeyValue> collect = stream.get()
                    .collect(Collectors.toList());
            List<MetricResult.KeyValue> result = new ArrayList<>();
            for (int i = 0; i < collect.size() - (smoothWindow - 1); i++) {
                List<Double> smoothValues = new ArrayList<>();
                for (int j = 0; j < smoothWindow; j++) {
                    if (collect.get(i + j).getValue() != null) {
                        smoothValues.add(collect.get(i + j).getValue());
                    }
                }
                OptionalDouble smoothValueOption = fn.apply(smoothValues.stream());
                Double smoothValue = smoothValueOption.isPresent() ? smoothValueOption.getAsDouble() : null;
                result.add(new MetricResult.KeyValue(collect.get(i).getTs(), smoothValue));
            }
            return result.stream();
        };
    }

    /**
     * Retrieve key-value pairs.
     *
     * @param result
     * @return
     */
    public Supplier<Stream<MetricResult.KeyValue>> getKeyValueStream(MetricResult.DataResult result) {
        if (result == null) {
            return null;
        }
        List<List> values = (List<List>) result.getValues();
        return () -> values.stream()
                .map(x -> (List<Object>) x)
                .map(l -> {
                    int ts = (Integer) l.get(0);
                    Double v = Double.valueOf((String) l.get(1));
                    if (v.isInfinite() || v.isNaN()) {
                        v = null;
                    }
                    return new MetricResult.KeyValue(ts, v);
                });
    }

    /**
     * Retrieve the maximum value.
     *
     * @param result
     * @return
     */
    public double getMax(List<MetricResult.DataResult> result) {
        if (!CollectionUtils.isEmpty(result)) {
            double max = Double.MIN_VALUE;
            for (MetricResult.DataResult dataResult : result) {
                List<List> values = dataResult.getValues();
                double value = values.stream().map(list -> list.get(1)).map(node -> {
                            try {
                                Double v = Double.valueOf((String) node);
                                if (v.isInfinite() || v.isNaN()) {
                                    return null;
                                } else {
                                    return v;
                                }
                            } catch (Exception e) {
                                return null;
                            }
                        }
                ).filter(Objects::nonNull).mapToDouble(x -> x).max().orElse(0);
                if (value >= max) {
                    max = value;
                }
            }
            BigDecimal b = new BigDecimal(max);
            return b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0D;
    }

    public List<Double> getTs(List<MetricResult.DataResult> dataList, double value) {
        List<Double> result = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(dataList)) {
            for (MetricResult.DataResult dr : dataList) {
                List<List> values = dr.getValues();
                for (List x : values) {
                    try {
                        double metricTs = (double) x.get(0);
                        int metricValue = (int) Double.parseDouble((String) x.get(1));
                        if (value == metricValue) {
                            result.add(metricTs);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retrieve the closest value.
     *
     * @param result
     * @return
     */
    public double getLatest(List<MetricResult.DataResult> result) {
        double resultValue = 0D;
        if (!CollectionUtils.isEmpty(result)) {
            long tsSec = 0L;
            for (MetricResult.DataResult dr : result) {
                List<List> values = dr.getValues();
                for (List x : values) {
                    try {
                        int metricTs = (int) x.get(0);
                        double metricValue = Double.parseDouble((String) x.get(1));
                        if (metricTs > tsSec && !Double.isNaN(metricValue) && !Double.isInfinite(metricValue)) {
                            resultValue = metricValue;
                            tsSec = metricTs;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return resultValue;
    }

    /**
     * Retrieve the minimum value.
     *
     * @param result
     * @return
     */
    public double getMin(List<MetricResult.DataResult> result) {
        if (!CollectionUtils.isEmpty(result)) {
            double min = Double.MAX_VALUE;
            for (MetricResult.DataResult dataResult : result) {
                List<List> values = dataResult.getValues();
                double value = values.stream().map(list -> list.get(1)).map(node -> {
                            try {
                                Double v = Double.valueOf((String) node);
                                if (v.isInfinite() || v.isNaN()) {
                                    return null;
                                } else {
                                    return v;
                                }
                            } catch (Exception e) {
                                return null;
                            }
                        }
                ).filter(Objects::nonNull).mapToDouble(x -> x).min().orElse(0);
                if (value <= min) {
                    min = value;
                }
            }
            BigDecimal b = new BigDecimal(min);
            return b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0D;
    }

    /**
     * Retrieve the average value.
     *
     * @param result
     * @return
     */
    public double getAvg(MetricResult.DataResult result) {
        if (result == null) {
            return 0D;
        }
        List<List> values = (List<List>) result.getValues();
        double value = values.stream().map(list -> list.get(1)).map(node -> {
                    try {
                        Double v = Double.valueOf((String) node);
                        if (v.isInfinite() || v.isNaN()) {
                            return null;
                        } else {
                            return v;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
        ).filter(Objects::nonNull).mapToDouble(value1 -> value1).average().orElse(0);
        return value;
    }

    /**
     * Retrieve the average value.
     *
     * @param result
     * @return
     */
    public double getAvg(List<MetricResult.DataResult> result) {
        if (!CollectionUtils.isEmpty(result)) {
            if (result.get(0) == null) {
                return 0D;
            }
            List<Double> dataList = Lists.newArrayList();
            for (MetricResult.DataResult dataResult : result) {
                List<List> values = dataResult.getValues();
                values.stream().map(list -> list.get(1)).map(node -> {
                            try {
                                Double v = Double.valueOf((String) node);
                                if (v.isInfinite() || v.isNaN()) {
                                    return null;
                                } else {
                                    return v;
                                }
                            } catch (Exception e) {
                                return null;
                            }
                        }
                ).filter(Objects::nonNull).forEach(dataList::add);
            }
            double avg = dataList.stream().mapToDouble(v -> v).average().orElse(0D);
            BigDecimal b = new BigDecimal(avg);
            return b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        return 0D;
    }

    public double getValue(List<MetricResult.DataResult> result) {
        if (!CollectionUtils.isEmpty(result)) {
            List<Object> values = (List<Object>) result.get(0).getValues();
            return Double.parseDouble((String) values.get(1));
        }
        return 0D;
    }

    /**
     * Calculate the query interval(step)
     *
     * @param start Start timestamp(In seconds)
     * @param end   End timestamp(In seconds)
     * @return step (In seconds)
     */
    public int getStep(long start, long end) {
        long dis = end - start;
        int daySec = 24 * 60 * 60;
        int step;
        if (dis <= daySec / 4) {
            // 6 hours.
            step = 60;
        } else if (dis <= daySec / 2) {
            // 12 hours.
            step = 120;
        } else if (dis <= daySec) {
            // 1 day.
            step = 120;
        } else if (dis <= 2 * daySec) {
            // 2 days.
            step = 600;
        } else if (dis <= 3 * daySec) {
            // 3 days.
            step = 600;
        } else if (dis <= 5 * daySec) {
            // 5 days.
            step = 1200;
        } else if (dis <= 6 * daySec) {
            // 6 days.
            step = 3600;
        } else if (dis <= 7 * daySec) {
            step = 3600;
        } else {
            step = 7200;
        }
        return step;
    }

    /**
     * Retrieve the average value of the lower edge of the sawtooth.
     *
     * @param result
     * @return
     */
    public Double getAvgOfBelowSerrationsOrNull(MetricResult.DataResult result) {
        if (result == null) {
            return null;
        }
        List<MetricResult.KeyValue> data = getKeyValueStream(result)
                .get()
                .collect(Collectors.toList());
        List<Double> belowSerrations = new ArrayList<>();
        for (int i = 1; i < data.size() - 1; i++) {
            MetricResult.KeyValue left = data.get(i - 1);
            MetricResult.KeyValue cur = data.get(i);
            MetricResult.KeyValue right = data.get(i + 1);
            if (cur.getValue() != null && left.getValue() != null && right.getValue() != null
                    && cur.getValue() < right.getValue() && left.getValue() > cur.getValue()) {
                belowSerrations.add(cur.getValue());
            }
        }
        OptionalDouble average = belowSerrations.stream().mapToDouble(x -> x)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return null;
        }
    }
}
