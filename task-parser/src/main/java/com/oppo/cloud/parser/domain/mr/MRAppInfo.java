package com.oppo.cloud.parser.domain.mr;

import com.oppo.cloud.parser.domain.mr.event.TaskInfo;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MRAppInfo {
    private String jobId;
    private String errorInfo;
    private String username;
    private String jobName;
    private String jobQueueName;
    private Long submitTime;
    private Long launchTime;
    private Integer totalMaps;
    private Integer totalReduces;
    private Integer failedMaps;
    private Integer failedReduces;
    private Integer succeededMaps;
    private Integer succeededReduces;
    private Integer killedMaps;
    private Integer killedReduces;
    private Long finishTime;
    private String jobStatus;
    private Map<String, String> confMap;

    private Map<String, TaskInfo> tasksMap = new HashMap<>();
    private Map<String, Map<String, Long>> totalCounters;
    private List<MRTaskAttemptInfo> mapList;
    private List<MRTaskAttemptInfo> reduceList;
    private SpeculationInfo speculationInfo;

}
