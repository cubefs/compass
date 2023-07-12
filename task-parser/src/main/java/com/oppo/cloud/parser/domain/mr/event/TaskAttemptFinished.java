package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskAttemptFinished {
    private String taskid;
    private String attemptId;
    private String taskType;
    private String taskStatus;
    private Long finishTime;
    private String rackname;
    private String hostname;
    private String state;
    private JhCounters jhCounters;
}
