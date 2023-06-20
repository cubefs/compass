package com.oppo.cloud.portal.domain.realtime;

import com.oppo.cloud.model.FlinkTaskDiagnosis;
import lombok.Data;

import java.util.List;

@Data
public class DiagnosisReportResp {
    FlinkTaskDiagnosis flinkTaskDiagnosis;
    List<String> reports;
}
