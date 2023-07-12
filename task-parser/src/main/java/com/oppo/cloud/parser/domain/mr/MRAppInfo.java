package com.oppo.cloud.parser.domain.mr;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MRAppData {
    private String jobId;

    private String errorInfo;
    private String username;

    private String jobname;

    private String jobQueueName;

    private long submitTime;

    private long launchTime;

    private long finishTime;

    private String jobStatus;

    private Map<String, String> confMap;

    private Map<String, Map<String, Long>> totalCounters;

    private List<MRTaskAttemptInfo> mapList;

    private List<MRTaskAttemptInfo> reduceList;

    private SpeculationInfo speculationInfo;

}
