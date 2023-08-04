package com.oppo.cloud.common.domain.mr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MRGCGraph {

    private Long taskId;
    private Double cpuTime;
    private Double gcTime;
    private String graphType;

}
