package com.oppo.cloud.common.domain.flink.report;

import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import lombok.Data;

import java.util.List;

@Data
public class DiagnosisRuleLine {
    String label;
    List<MetricResult.DataResult> data;
}
