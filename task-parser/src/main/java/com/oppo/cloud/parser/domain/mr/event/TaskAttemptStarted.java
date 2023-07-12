package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskAttemptStarted {
    private String taskid;
    private String taskType;
    private String attemptId;
    private Long startTime;
    private String trackerName;
    private Integer httpPort;
    private Integer shufflePort;
    private String containerId;
    private String locality;
    private String avataar;
}
