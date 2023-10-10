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

package com.oppo.cloud.flink.domain.diagnosis;

import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.flink.advice.IAdviceRule;
import com.oppo.cloud.flink.advice.DiagnosisDoctor;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisParam;
import com.oppo.cloud.flink.service.impl.FlinkDiagnosisMetricsServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Diagnosis context.
 */
@Data
@Builder
@AllArgsConstructor
public class DiagnosisContext {
    public DiagnosisContext(RcJobDiagnosis rcJobDiagnosis, long start, long end,
                            FlinkDiagnosisMetricsServiceImpl metricClient, DiagnosisFrom from) {
        this.metrics = new WeakHashMap<>();
        this.messages = new HashMap<>();
        if (rcJobDiagnosis != null) {
            this.rcJobDiagnosis = rcJobDiagnosis;
        } else {
            this.rcJobDiagnosis = new RcJobDiagnosis();
        }
        this.start = start;
        this.end = end;
        this.metricsClient = metricClient;
        this.from = from;
    }

    /**
     * Diagnosis source.
     */
    private DiagnosisFrom from;
    /**
     * Diagnosis data.
     */
    private RcJobDiagnosis rcJobDiagnosis;
    /**
     * Diagnosis metrics.
     */
    private Map<String, List<MetricResult.DataResult>> metrics;
    /**
     * Diagnosis message.
     */
    private Map<DiagnosisParam, Object> messages;
    /**
     * Start time in seconds.
     */
    private Long start;
    /**
     * End time in seconds.
     */
    private Long end;
    /**
     * Metric retrieval service.
     */
    FlinkDiagnosisMetricsServiceImpl metricsClient;
    /**
     * Scaling-in rule.
     */
    List<IAdviceRule> decrRules = null;
    /**
     * Scaling-out rule.
     */
    List<IAdviceRule> incrRules = null;
    /**
     * Basic rule.
     */
    List<IAdviceRule> attentionRules = null;
    /**
     * Diagnosis doctor.
     */
    private DiagnosisDoctor doctor;
    /**
     * Whether to stop the diagnosis.
     */
    private Boolean stopResourceDiagnosis = false;
    /**
     * Diagnosis advice.
     */
    private List<RcJobDiagnosisAdvice> advices;
}
