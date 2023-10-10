package com.oppo.cloud.flink.service;

import org.opensearch.action.update.UpdateResponse;

public interface FlinkOpenSearchService {

    /**
     * Insert or update item with document id
     */
    UpdateResponse insertOrUpdate(String index, String id, Object document) throws Exception;
}
