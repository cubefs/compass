package com.oppo.cloud.diagnosis.domain.diagnosis;

import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.advice.IAdviceRule;
import com.oppo.cloud.diagnosis.advice.DiagnosisDoctor;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisFrom;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisParam;
import com.oppo.cloud.diagnosis.service.impl.RealtimeDiagnosisMetricsServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 诊断上下文
 *
 */
@Data
@Builder
@AllArgsConstructor
public class DiagnosisContext {
    public DiagnosisContext(RcJobDiagnosis rcJobDiagnosis, long start, long end,
                            RealtimeDiagnosisMetricsServiceImpl metricClient, EDiagnosisFrom from) {
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
    private EDiagnosisFrom from;
    private RcJobDiagnosis rcJobDiagnosis;
    private Map<String, List<MetricResult.DataResult>> metrics;
    private Map<EDiagnosisParam, Object> messages;
    // 开始时间秒
    private Long start;
    // 结束时间秒
    private Long end;
    RealtimeDiagnosisMetricsServiceImpl metricsClient;
    List<IAdviceRule> cutRules = null;
    List<IAdviceRule> growRules = null;
    List<IAdviceRule> attentionRules = null;
    private DiagnosisDoctor doctor;
    private Boolean elastic = false;
    private Boolean stopResourceDiagnosis = false;
    private String clusterId;
    private String namespace;
    private String clusterConfig;
    private List<RcJobDiagnosisAdvice> advices;
}
