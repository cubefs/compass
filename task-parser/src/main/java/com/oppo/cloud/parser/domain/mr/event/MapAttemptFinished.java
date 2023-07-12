package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.List;

@Data
public class MapAttemptFinished {
    private String taskid;
    private String attemptId;
    private String taskType;
    private String taskStatus;
    private Long mapFinishTime;
    private Long finishTime;
    private String hostname;
    private Integer port;
    private String rackname;
    private String state;
    private JhCounters counters;
    private List<Integer> clockSplits;
    private List<Integer> cpuUsages;
    private List<Integer> vMemKbytes;
    private List<Integer> physMemKbytes;
}
