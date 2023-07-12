package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JobInfoChange {
    private String jobid;
    private Long submitTime;
    private Long launchTime;
}
