package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JobInit {
    private String jobid;
    private Long launchTime;
    private Integer totalMaps;
    private Integer totalReduces;
    private String jobStatus;
    private Boolean uberized;
}
