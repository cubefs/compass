package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.Map;

@Data
public class TaskInfo {
    private String taskId;

    private long startTime;

    private long finishTime;

    private String taskType;

    private String splitLocations;

    private JhCounters counters;

    private String status;

    private String error;

    private String failedDueToAttemptId;

    private String successfulAttemptId;

    private Map<String, TaskAttemptInfo> attemptsMap;
}
