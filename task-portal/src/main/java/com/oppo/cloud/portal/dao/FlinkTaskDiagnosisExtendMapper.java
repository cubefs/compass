package com.oppo.cloud.portal.dao;

import com.oppo.cloud.mapper.FlinkTaskDiagnosisMapper;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;
import com.oppo.cloud.portal.domain.realtime.DiagnosisAdviceListReq;
import com.oppo.cloud.portal.domain.realtime.DiagnosisGeneralViewReq;
import com.oppo.cloud.portal.domain.realtime.GeneralViewNumberDto;

import java.util.List;

public interface FlinkTaskDiagnosisExtendMapper extends FlinkTaskDiagnosisMapper {
    GeneralViewNumberDto getGeneralViewNumber(DiagnosisGeneralViewReq request);
    List<GeneralViewNumberDto> getGeneralViewTrend(DiagnosisGeneralViewReq request);
    List<RealtimeTaskDiagnosis> page(DiagnosisAdviceListReq request);

}
