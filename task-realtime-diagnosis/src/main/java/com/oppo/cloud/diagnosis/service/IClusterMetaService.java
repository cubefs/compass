package com.oppo.cloud.diagnosis.service;



import java.util.List;
import java.util.Map;

/**
 * YANR、SPARK集群信息更新、获取
 */
public interface IClusterMetaService {


    /**
     * 获取yarn rm列表
     */
    Map<String,String> getYarnClusters();
}
