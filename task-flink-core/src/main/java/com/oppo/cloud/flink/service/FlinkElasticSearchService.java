package com.oppo.cloud.flink.service;

import org.elasticsearch.action.update.UpdateResponse;

public interface FlinkElasticSearchService {

    /**
     * 插入或更新Es数据库
     */
    UpdateResponse insertOrUpDateEs(String index, String id, Object document) throws Exception;
}
