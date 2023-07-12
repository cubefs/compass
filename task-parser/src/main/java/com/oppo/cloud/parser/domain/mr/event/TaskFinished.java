package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskFinished {
    private String taskid;
    private String taskType;
    private Long finishTime;
    private String status;
    private JhCounters counters;
    private String successfulAttemptId;
}
