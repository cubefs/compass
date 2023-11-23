package com.oppo.cloud.common.domain;

import lombok.Data;

@Data
public class LogMessage {
    private String id;
    private String index;
    private String logType;
    private String rawLog;
}
