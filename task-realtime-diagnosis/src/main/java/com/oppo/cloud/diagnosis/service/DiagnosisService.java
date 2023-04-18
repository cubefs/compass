package com.oppo.cloud.diagnosis.service;

import com.oppo.cloud.common.domain.flink.enums.EDiagnosisFrom;
import com.oppo.cloud.model.RealtimeTaskApp;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;

public interface DiagnosisService{
    RealtimeTaskDiagnosis diagnosisApp(RealtimeTaskApp realtimeTaskApp, long start, long end, EDiagnosisFrom from);
    void diagnosisAllApp(long start, long end, EDiagnosisFrom from);
    void diagnosisAppHourly(long start, long end, EDiagnosisFrom from);
}