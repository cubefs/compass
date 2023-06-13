package com.oppo.cloud.portal.domain.realtime;

import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;
import com.oppo.cloud.portal.domain.diagnose.DiagnoseReport;
import com.oppo.cloud.portal.domain.diagnose.Item;
import lombok.Data;

import java.util.List;

@Data
public class DiagnosisReportResp {
    RealtimeTaskDiagnosis realtimeTaskDiagnosis;
    List<String> reports;
}
