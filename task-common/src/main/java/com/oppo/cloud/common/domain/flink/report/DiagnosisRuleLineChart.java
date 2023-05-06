package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 诊断规则线状图表
 */
@Data
public class DiagnosisRuleLineChart implements IDiagnosisRuleChart{
    /**
     * 图表类型
     */
    String type = "Line";
    /**
     * 标题
     */
    String title;
    /**
     * y轴单位
     */
    String yAxisUnit;
    /**
     * y轴值类型
     */
    String yAxisValueType;
    /**
     * 线
     */
    DiagnosisRuleLine line;
    /**
     * 常数线
     */
    Map<String,Double> constLines;
    /**
     * y轴最大值
     */
    Double yAxisMax;
    /**
     * y轴最小值
     */
    Double yAxisMin;
}
