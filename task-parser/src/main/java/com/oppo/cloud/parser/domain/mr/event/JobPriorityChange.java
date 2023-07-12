package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JobPriorityChange {
    private String jobid;
    private String priority;
}
