package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JobUnsuccessfulCompletion {
    private String jobid;
    private Long finishTime;
    private Integer finishedMaps;
    private Integer finishedReduces;
    private String jobStatus;
    private String diagnostics;
    private Integer failedMaps;
    private Integer failedReduces;
    private Integer killedMaps;
    private Integer killedReduces;
}
