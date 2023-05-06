package com.oppo.cloud.diagnosis.domain.diagnosis;

import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.diagnosis.advice.IAdviceRule;
import com.oppo.cloud.diagnosis.advice.DiagnosisDoctor;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisParam;
import com.oppo.cloud.diagnosis.service.impl.FlinkDiagnosisMetricsServiceImpl;
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
     * 诊断来源
     */
    private DiagnosisFrom from;
    /**
     * 诊断数据
     */
    private RcJobDiagnosis rcJobDiagnosis;
    /**
     * 诊断指标
     */
    private Map<String, List<MetricResult.DataResult>> metrics;
    /**
     * 诊断消息
     */
    private Map<DiagnosisParam, Object> messages;
    /**
     * 开始时间秒
     */
    private Long start;
    /**
     * 结束时间秒
     */
    private Long end;
    /**
     * 指标获取服务
     */
    FlinkDiagnosisMetricsServiceImpl metricsClient;
    /**
     * 缩容规则
     */
    List<IAdviceRule> decrRules = null;
    /**
     * 扩容规则
     */
    List<IAdviceRule> incrRules = null;
    /**
     * 基本规则
     */
    List<IAdviceRule> attentionRules = null;
    /**
     * 诊断医生
     */
    private DiagnosisDoctor doctor;
    /**
     * 是否停止诊断
     */
    private Boolean stopResourceDiagnosis = false;
    /**
     * 诊断建议
     */
    private List<RcJobDiagnosisAdvice> advices;
}
