package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class AMStarted {
    private String applicationAttemptId;
    private Long startTime;
    private String containerId;
    private String nodeManagerHost;
    private Integer nodeManagerPort;
    private Integer nodeManagerHttpPort;

}
