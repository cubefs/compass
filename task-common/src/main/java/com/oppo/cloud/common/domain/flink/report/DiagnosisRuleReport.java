package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;

@Data
public class DiagnosisRuleReport {
    List<IDiagnosisRuleChart> IDiagnosisRuleCharts;
    String title;
    String conclusion;
}
