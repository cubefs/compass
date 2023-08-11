package com.oppo.cloud.flink.service.impl;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.flink.service.FlinkElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class FlinkElasticSearchServiceImpl implements FlinkElasticSearchService {

    @Resource(name = "flinkElasticClient")
    private RestHighLevelClient restHighLevelClient;

    /**
     * 插入或更新Es数据库
     */
    public UpdateResponse insertOrUpDateEs(String index, String id, Object document) throws Exception {
        String json = "";
        try {
            json = JSON.toJSONString(document);
            log.info("json:" + json);
        } catch (Exception e) {
            throw new Exception(String.format("insertOrUpDateEs writeValueAsString failed:%s", e.getMessage()));
        }
        UpdateResponse updateResponse;
        try {
            UpdateRequest request =
                    new UpdateRequest(index, id).doc(json, XContentType.JSON).upsert(json, XContentType.JSON);
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new Exception(String.format("insertOrUpDateEs update failed:%s", e.getMessage()));
        }
        return updateResponse;
    }
}
