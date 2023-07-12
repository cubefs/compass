package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskAttemptInfo {
    private String attemptId;
    private Long startTime;
    private Long finishTime;
    private Long shuffleFinishTime;
    private Long sortFinishTime;
    private Long mapFinishTime;
    private String error;
    private String status;
    private String state;
    private String taskType;
    private String trackerName;
    private JhCounters counters;
    private int httpPort;
    private int shufflePort;
    private String hostname;
    private int port;
    private String rackname;
    private String containerId;
}
