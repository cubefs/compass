package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisGeneralViewQuery {
    // 开始时间戳
    LocalDateTime startTs;
    // 结束时间戳
    LocalDateTime endTs;
}
