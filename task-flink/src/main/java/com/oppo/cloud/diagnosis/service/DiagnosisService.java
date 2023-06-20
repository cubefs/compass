package com.oppo.cloud.diagnosis.service;

import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskDiagnosis;

public interface DiagnosisService{
    FlinkTaskDiagnosis diagnosisApp(FlinkTaskApp flinkTaskApp, long start, long end, DiagnosisFrom from);
    void diagnosisAllApp(long start, long end, DiagnosisFrom from);
    void diagnosisAppHourly(long start, long end, DiagnosisFrom from);
}