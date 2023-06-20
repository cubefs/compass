package com.oppo.cloud.diagnosis.service.impl;

import com.oppo.cloud.common.domain.cluster.hadoop.YarnConf;
import com.oppo.cloud.common.util.YarnUtil;
import com.oppo.cloud.diagnosis.config.ClusterConfig;
import com.oppo.cloud.diagnosis.service.IClusterMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 更新、获取YANR、SPARK集群信息
 */
@Slf4j
@Service
public class ClusterMetaServiceImpl implements IClusterMetaService {

    @Autowired
    private ClusterConfig config;

    /**
     * 获取yarn rm列表
     */
    @Override
    public Map<String, String> getYarnClusters() {
        List<YarnConf> yarnConfList = config.getYarn();
        return YarnUtil.getYarnClusters(yarnConfList);
    }
}
