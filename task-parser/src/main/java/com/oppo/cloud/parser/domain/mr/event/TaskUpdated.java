package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class TaskUpdated {
    private String taskid;
    private Long finishTime;
}
