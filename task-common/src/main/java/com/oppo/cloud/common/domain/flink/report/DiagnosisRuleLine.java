package com.oppo.cloud.common.domain.flink.report;

import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import lombok.Data;

import java.util.List;

/**
 * 诊断规则线状图
 */
@Data
public class DiagnosisRuleLine {
    /**
     * 标签
     */
    String label;
    /**
     * 指标曲线
     */
    List<MetricResult.DataResult> data;
}
