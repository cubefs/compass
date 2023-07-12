package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskStarted {
    private String taskid;
    private String taskType;
    private Long startTime;
    private String splitLocations;
}
