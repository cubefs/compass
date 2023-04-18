package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

@Data
public class DiagnosisGeneralViewNumberResp {
    GeneralViewNumberDto generalViewNumberDto;
    GeneralViewNumberDto generalViewNumberDtoDay1Before;
    GeneralViewNumberDto generalViewNumberDtoDay7Before;

}
