package com.oppo.cloud.parser.domain.mr;

import lombok.Data;

import java.util.Map;

@Data
public class MRTaskAttemptInfo {

    private int taskId;

    private String attemptId;

    private String taskStatus;
    private long startTime;

    private long finishTime;

    private long shuffleFishTime;

    private long sortFinishTime;

    private long elapsedTime;

    private String error;

    private Map<String, Map<String, Long>> counters;
}
