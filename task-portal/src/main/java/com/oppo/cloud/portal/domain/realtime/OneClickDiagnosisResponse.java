package com.oppo.cloud.portal.domain.realtime;

import com.oppo.cloud.model.FlinkTaskDiagnosis;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OneClickDiagnosisResponse {
    @ApiModelProperty(value = "诊断状态(failed, succeed, processing)")
    private String status;
    @ApiModelProperty(value = "异常信息")
    private String errorMsg;
    @ApiModelProperty(value = "实时诊断结果")
    private FlinkTaskDiagnosis flinkTaskDiagnosis;
}
