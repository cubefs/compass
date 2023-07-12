package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JobFinished {
    private String jobid;
    private Long finishTime;
    private Integer finishedMaps;
    private Integer finishedReduces;
    private Integer failedMaps;
    private Integer failedReduces;
    private JhCounters totalCounters;
    private JhCounters mapCounters;
    private JhCounters reduceCounters;
    private Integer killedMaps;
    private Integer killedReduces;
}
