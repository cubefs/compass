package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DiagnosisRuleBarChart implements IDiagnosisRuleChart {
    String title;
    String yAxisUnit;
    List<DiagnosisRulePoint> bars;
    Double yAxisMax;
    Double yAxisMin;
}
