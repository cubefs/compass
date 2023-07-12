package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JobQueueChange {
    private String jobid;
    private String jobQueueName;
}
