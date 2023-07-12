package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskFailed {
    private String taskid;
    private String taskType;
    private Long finishTime;
    private String error;
    private String failedDueToAttempt;
    private String status;
    private JhCounters counters;
}
