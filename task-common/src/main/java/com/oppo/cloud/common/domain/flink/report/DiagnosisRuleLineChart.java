package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DiagnosisRuleLineChart implements IDiagnosisRuleChart{
    String type = "Line";
    String title;
    String yAxisUnit;
    String yAxisValueType;
    DiagnosisRuleLine line;
    Map<String,Double> constLines;
    Double yAxisMax;
    Double yAxisMin;
}
