/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
