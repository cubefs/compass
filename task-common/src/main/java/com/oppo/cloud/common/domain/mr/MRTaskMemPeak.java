package com.oppo.cloud.common.domain.mr;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MRTaskMemPeak {
    private Integer taskId;
    private Integer peakUsed;
}
