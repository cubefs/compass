package com.oppo.cloud.common.domain.mr;

import lombok.Data;

import java.util.List;

@Data
public class MRSpeculativeAbnormal {
    private Boolean abnormal;
    private List<Long> elapsedTime;
    private List<String> taskAttemptIds;
}
