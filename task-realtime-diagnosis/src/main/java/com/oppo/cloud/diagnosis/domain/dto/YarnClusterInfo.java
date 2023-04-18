package com.oppo.cloud.diagnosis.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * Yarn集群信息
 */
@Data
public class YarnClusterInfo {
    /**
     * rm地址
     */
    private List<String> resourceManager;
    /**
     * jobHistoryServer地址
     */
    private List<String> jobHistoryServer;
    /**
     * 集群名称
     */
    private String clusterName;

}
