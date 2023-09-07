package com.oppo.cloud.flink.service;

import org.opensearch.action.update.UpdateResponse;

public interface FlinkOpenSearchService {

    /**
     * 插入或更新
     */
    UpdateResponse insertOrUpDate(String index, String id, Object document) throws Exception;
}
