package com.oppo.cloud.diagnosis.config;


import com.oppo.cloud.diagnosis.domain.dto.YarnClusterInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * YARN、SPARK集群配置
 */
@Configuration
@ConfigurationProperties(prefix = "cluster")
@Data
public class ClusterConfig {
    /**
     * yarn集群信息
     */
    private List<YarnClusterInfo> yarn;
    /**
     * resourcemanager port
     */
    private String rmPort;
}
