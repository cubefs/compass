package com.oppo.cloud.common.domain.cluster.hadoop;

import lombok.Data;

import java.util.List;

@Data
public class YarnConf {

    private String clusterName;
    private List<String> resourceManager;

    private String jobHistoryServer;

}
