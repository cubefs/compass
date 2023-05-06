package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;

/**
 * 诊断规则报告
 */
@Data
public class DiagnosisRuleReport {
    /**
     * 诊断规则图表
     */
    List<IDiagnosisRuleChart> IDiagnosisRuleCharts;
    /**
     * 标题
     */
    String title;
    /**
     * 结论
     */
    String conclusion;
}
