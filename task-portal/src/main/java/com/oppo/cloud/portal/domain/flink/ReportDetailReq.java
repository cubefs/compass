package com.oppo.cloud.portal.domain.flink;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReportDetailReq {

    @ApiModelProperty(value = "flink task analysis id")
    private String id;
}
