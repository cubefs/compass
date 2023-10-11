package com.oppo.cloud.flink.service.impl;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.flink.service.FlinkOpenSearchService;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class FlinkOpenSearchServiceImpl implements FlinkOpenSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * Upsert item
     */
    @Override
    public UpdateResponse insertOrUpdate(String index, String id, Object document) throws Exception {
        String json = "";
        try {
            json = JSON.toJSONString(document);
            log.info("json:" + json);
        } catch (Exception e) {
            throw new Exception(String.format("insertOrUpDate writeValueAsString failed:%s", e.getMessage()));
        }
        UpdateResponse updateResponse;
        try {
            UpdateRequest request =
                    new UpdateRequest(index, id).doc(json, XContentType.JSON).upsert(json, XContentType.JSON);
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new Exception(String.format("insertOrUpDate update failed:%s", e.getMessage()));
        }
        return updateResponse;
    }
}
