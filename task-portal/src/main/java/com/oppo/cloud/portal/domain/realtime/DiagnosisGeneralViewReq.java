package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class DiagnosisGeneralViewReq {
    // 开始时间戳秒
    LocalDateTime startTs;
    // 结束时间戳秒
    LocalDateTime endTs;
}
