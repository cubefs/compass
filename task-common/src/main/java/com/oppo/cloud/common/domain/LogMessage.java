package com.oppo.cloud.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String id;
    private String index;
    private String logType;
    private String rawLog;
}
