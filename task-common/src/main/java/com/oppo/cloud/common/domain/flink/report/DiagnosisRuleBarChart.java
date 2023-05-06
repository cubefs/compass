package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 诊断规则柱状图
 */
@Data
public class DiagnosisRuleBarChart implements IDiagnosisRuleChart {
    /**
     * 图表类型
     */
    String type = "Bar";
    /**
     * 标题
     */
    String title;
    /**
     * y轴单位
     */
    String yAxisUnit;
    /**
     * 柱状图
     */
    List<DiagnosisRulePoint> bars;
    /**
     * y轴最大值
     */
    Double yAxisMax;
    /**
     * y轴最小值
     */
    Double yAxisMin;
}
