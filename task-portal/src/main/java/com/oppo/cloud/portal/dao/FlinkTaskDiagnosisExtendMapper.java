package com.oppo.cloud.portal.dao;

import com.oppo.cloud.mapper.FlinkTaskDiagnosisMapper;
import com.oppo.cloud.model.FlinkTaskDiagnosis;
import com.oppo.cloud.portal.domain.realtime.DiagnosisAdviceListReq;
import com.oppo.cloud.portal.domain.realtime.DiagnosisGeneralViewQuery;
import com.oppo.cloud.portal.domain.realtime.GeneralViewNumberDto;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlinkTaskDiagnosisExtendMapper extends FlinkTaskDiagnosisMapper {
    GeneralViewNumberDto getGeneralViewNumber(DiagnosisGeneralViewQuery request);
    List<GeneralViewNumberDto> getGeneralViewTrend(@Param("diagnosisEndTimes") List<LocalDateTime> diagnosisEndTimes);
    List<LocalDateTime> getDiagnosisDates(DiagnosisGeneralViewQuery request);
    List<FlinkTaskDiagnosis> page(DiagnosisAdviceListReq request);
    LocalDateTime getDiagnosisTime(DiagnosisGeneralViewQuery request);

}
