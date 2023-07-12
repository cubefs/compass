package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.Map;

@Data
public class JobSubmitted {
    private String jobid;
    private String jobName;
    private String userName;
    private Long submitTime;
    private String jobConfPath;
    private Map<String,String> acls;
    private String jobQueueName;
    private String workflowId;
    private String workflowName;
    private String workflowNodeName;
    private String workflowAdjacencies;
    private String workflowTags;
}
