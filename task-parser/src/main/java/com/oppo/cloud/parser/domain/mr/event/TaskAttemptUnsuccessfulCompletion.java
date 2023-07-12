package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.List;

@Data
public class TaskAttemptUnsuccessfulCompletion {
    private String taskid;
    private String taskType;
    private String attemptId;
    private Long finishTime;
    private String hostname;
    private Integer port;
    private String rackname;
    private String status;
    private String error;
    private JhCounters counters;
    private List<Integer> clockSplits;
    private List<Integer> cpuUsages;
    private List<Integer> vMemKbytes;
    private List<Integer> physMemKbytes;
}
