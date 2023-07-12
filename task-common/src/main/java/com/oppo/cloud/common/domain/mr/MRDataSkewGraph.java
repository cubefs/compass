package com.oppo.cloud.common.domain.mr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MRDataSkewGraph {
    private Long taskId;
    private Long dataSize;
    private String graphType;
}
