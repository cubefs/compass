package com.oppo.cloud.diagnosis.config;


import com.oppo.cloud.common.domain.cluster.hadoop.NameNodeConf;
import com.oppo.cloud.common.domain.cluster.hadoop.SparkConf;
import com.oppo.cloud.common.domain.cluster.hadoop.YarnConf;
import com.oppo.cloud.diagnosis.domain.dto.YarnClusterInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


/**
 * YARN、SPARK集群配置
 */
@Configuration
@ConfigurationProperties(prefix = "hadoop")
@Data
public class ClusterConfig {
    private List<NameNodeConf> namenodes;

    private List<YarnConf> yarn;

    private SparkConf spark;
}
