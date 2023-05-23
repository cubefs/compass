package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

@Data
public class OneClickDiagnosisRequest {
    /**
     * appid
     */
    private String appId;
    /**
     * 时间戳秒
     */
    private Long end;
    /**
     * 时间戳秒
     */
    private Long  start;
}
