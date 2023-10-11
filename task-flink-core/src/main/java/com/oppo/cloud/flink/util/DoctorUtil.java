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

import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.TM_CPU_USAGE_RATE;
import static com.oppo.cloud.common.domain.flink.enums.DiagnosisParam.*;


@Slf4j
@Component
public class DoctorUtil {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getCpuLowTarget(DiagnosisContext context) {
        Double lowTarget = cons.getTmCpuUsageCutTarget();
        if (context.getMessages().get(CpuLowTarget) != null) {
            lowTarget = (Double) context.getMessages().get(CpuLowTarget);
            lowTarget = lowTarget / 100;
        }
        return lowTarget;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getCpuLowThreshold(DiagnosisContext context) {
        Double lowThreshold = cons.getTmCpuUsageCutThreshold();
        if (context.getMessages().get(CpuLowThreshold) != null) {
            lowThreshold = (Double) context.getMessages().get(CpuLowThreshold);
            lowThreshold = lowThreshold / 100;
        }
        return lowThreshold;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getCpuHighThreshold(DiagnosisContext context) {
        Double highThreshold = cons.getTmCpuUsageGrowThreshold();
        if (context.getMessages().get(CpuHighThreshold) != null) {
            highThreshold = (Double) context.getMessages().get(CpuHighThreshold);
            highThreshold = highThreshold / 100;
        }
        return highThreshold;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getCpuHighTarget(DiagnosisContext context) {
        Double highTarget = cons.getTmCpuUsageGrowTarget();
        if (context.getMessages().get(CpuHighTarget) != null) {
            highTarget = (Double) context.getMessages().get(CpuHighTarget);
            highTarget = highTarget / 100;
        }
        return highTarget;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getMemLowThreshold(DiagnosisContext context) {
        Double lowThreshold = cons.tmMemUsageLowThreshold;
        if (context.getMessages().get(MemoryLowThreshold) != null) {
            lowThreshold = (Double) context.getMessages().get(MemoryLowThreshold);
            lowThreshold = lowThreshold / 100;
        }
        return lowThreshold;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getMemLowTarget(DiagnosisContext context) {
        Double lowTarget = cons.tmMemUsageLowTarget;
        if (context.getMessages().get(MemoryLowTarget) != null) {
            lowTarget = (Double) context.getMessages().get(MemoryLowTarget);
            lowTarget = lowTarget / 100;
        }
        return lowTarget;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getMemHighThreshold(DiagnosisContext context) {
        Double highThreshold = cons.tmMemUsageHighThreshold;
        if (context.getMessages().get(MemoryHighThreshold) != null) {
            highThreshold = (Double) context.getMessages().get(MemoryHighThreshold);
            highThreshold = highThreshold / 100;
        }
        return highThreshold;
    }

    /**
     * 0-1 Range.
     * @param context
     * @return
     */
    public Double getMemHighTarget(DiagnosisContext context) {
        Double highTarget = cons.tmMemUsageHighTarget;
        if (context.getMessages().get(MemoryHighTarget) != null) {
            highTarget = (Double) context.getMessages().get(MemoryHighTarget);
            highTarget = highTarget / 100;
        }
        return highTarget;
    }

    public String addSemicolon(String msg) {
        if (msg != null) {
            if (!msg.equals("")
                    && !msg.endsWith(";")) {
                return msg + ";";
            }
        }
        return msg;
    }


    public String addComma(String msg) {
        if (msg != null) {
            if (!msg.equals("")
                    && !msg.endsWith(",")) {
                return msg + ",";
            }
        }
        return msg;
    }

    /**
     * Retrieve scaling change rate.
     * @param context
     * @return
     */
    public Double getGrowChangeRate(DiagnosisContext context) {
        if (context == null) {
            return null;
        }
        try {
            Double changeRate = (Double) context.getMessages().get(GrowCpuChangeRate);
            if (changeRate != null) {
                return changeRate;
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        if (rcJobDiagnosis == null) {
            return null;
        }
        Double highTarget = getCpuHighTarget(context);
        // Calculate change rate based on CPU utilization.
        List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_CPU_USAGE_RATE);
        if (cpuUsageList != null && cpuUsageList.size() != 0) {
            int step = monitorMetricUtil.getStep(context.getStart(), context.getEnd());
            float duration = cons.getTmPeakHighTimeThreshold();
            float ratio = cons.getCpuUsageAccHighTimeRate();
            float durationPointNum = duration / step;
            float ratioPointNum = (context.getEnd() - context.getStart()) * ratio / step;
            int batchHighPointNum = (int) Math.ceil(Math.min(durationPointNum, ratioPointNum));
            Optional<Double> maxRateOpt = cpuUsageList.stream().map(x -> {
                                OptionalDouble average = monitorMetricUtil.getFlatKeyValueStream(x)
                                        .get()
                                        .sorted(Comparator.comparingDouble(MetricResult.KeyValue::getValue).reversed())
                                        .limit(batchHighPointNum)
                                        .mapToDouble(MetricResult.KeyValue::getValue)
                                        .average();
                                log.debug("{} CPU Task Manager Top {} peak average {} raw data: {}", rcJobDiagnosis.getJobName(),
                                        batchHighPointNum, average.toString(), x);
                                if (average.isPresent()) {
                                    return average.getAsDouble();
                                } else {
                                    return null;
                                }
                            }
                    )
                    .filter(Objects::nonNull)
                    .max(Double::compareTo);
            if (maxRateOpt.isPresent()) {
                Double maxRate = maxRateOpt.get();
                Double unitMaxRate = maxRate;
                if (unitMaxRate > highTarget) {
                    return unitMaxRate / highTarget - 1;
                } else {
                    log.info(String.format("CPU utilization after peak shaving and normalization is less than %.2f.", highTarget));
                    return null;
                }
            }
        } else {
            log.error("{} cpu list metric is empty", context.getRcJobDiagnosis().getJobName());
        }
        return null;
    }

    /**
     * When the value of x ranges from negative infinity to 0, it returns positive infinity to 1.
     * When the value of x ranges from 0 to positive infinity, it returns from 1 to 0.
     *
     * @param x
     * @return
     */
    public double oneDivideExp(double x) {
        return 1 / Math.exp(x);
    }

    /**
     * When the value of x ranges from negative infinity to 0, it returns from 1 to 0.
     * When the value of x ranges from 0 to positive infinity, it returns from 0 to negative infinity.
     *
     * @param x
     * @return
     */
    public double oneSubExp(double x) {
        return 1 - Math.exp(x);
    }

    public String getMetricJobName(DiagnosisContext context) {
        return context.getRcJobDiagnosis().getJobName();
    }
}
