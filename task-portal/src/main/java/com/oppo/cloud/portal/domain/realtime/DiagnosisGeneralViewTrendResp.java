package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

import java.util.List;

@Data
public class DiagnosisGeneralViewTrendResp {
    List<GeneralViewNumberDto> trend;
}
