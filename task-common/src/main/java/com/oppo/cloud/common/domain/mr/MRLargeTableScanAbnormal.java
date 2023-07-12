package com.oppo.cloud.common.domain.mr;

import lombok.Data;

@Data
public class MRLargeTableScanAbnormal {
    private Boolean abnormal;
    private Long records;

}
