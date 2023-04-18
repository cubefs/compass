package com.oppo.cloud.portal.domain.realtime;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "诊断来源")
public class DiagnosisFromResp {
    int code;
    String name;
}
