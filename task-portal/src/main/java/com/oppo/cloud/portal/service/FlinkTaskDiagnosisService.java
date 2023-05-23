package com.oppo.cloud.portal.service;

import com.oppo.cloud.common.api.CommonPage;
import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;
import com.oppo.cloud.portal.domain.diagnose.oneclick.DiagnoseResult;
import com.oppo.cloud.portal.domain.realtime.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

public interface FlinkTaskDiagnosisService {
    CommonPage<RealtimeTaskDiagnosis> pageJobs(DiagnosisAdviceListReq req);
    DiagnosisGeneralViewNumberResp getGeneralViewNumber(@Validated @RequestBody DiagnosisGeneralViewReq request);
    DiagnosisGeneralViewTrendResp getGeneralViewTrend(@Validated @RequestBody DiagnosisGeneralViewReq request);
    DiagnosisGeneralVIewDistributeResp getGeneralViewDistribute(@Validated @RequestBody DiagnosisGeneralViewReq request);
    DiagnosisReportResp getReport(RealtimeTaskDiagnosis request);
    CommonStatus<OneClickDiagnosisResponse> diagnosis(OneClickDiagnosisRequest req);
}
