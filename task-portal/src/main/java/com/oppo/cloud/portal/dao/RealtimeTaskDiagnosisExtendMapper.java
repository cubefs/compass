package com.oppo.cloud.portal.dao;

import com.oppo.cloud.mapper.RealtimeTaskDiagnosisMapper;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;
import com.oppo.cloud.model.RealtimeTaskDiagnosisExample;
import com.oppo.cloud.portal.domain.realtime.DiagnosisAdviceListReq;
import com.oppo.cloud.portal.domain.realtime.DiagnosisGeneralViewNumberResp;
import com.oppo.cloud.portal.domain.realtime.DiagnosisGeneralViewReq;
import com.oppo.cloud.portal.domain.realtime.GeneralViewNumberDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface RealtimeTaskDiagnosisExtendMapper extends RealtimeTaskDiagnosisMapper {
    GeneralViewNumberDto getGeneralViewNumber(DiagnosisGeneralViewReq request);
    List<GeneralViewNumberDto> getGeneralViewTrend(DiagnosisGeneralViewReq request);
    List<RealtimeTaskDiagnosis> page(DiagnosisAdviceListReq request);

}
