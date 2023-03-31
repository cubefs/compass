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

package com.oppo.cloud.common.util.elastic;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 批量添加文档接口
 */
public class BulkApi {

    public static BulkResponse bulk(RestHighLevelClient client, final String index,
                                    final List<Map<String, Object>> documents) throws IOException {
        final BulkRequest bulkRequest = new BulkRequest();
        documents.forEach(document -> {
            final IndexRequest indexRequest = new IndexRequest(index);
            indexRequest.source(document);
            bulkRequest.add(indexRequest);
        });
        return client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    public static BulkResponse bulkJson(RestHighLevelClient client, final String index,
                                        final List<String> list) throws IOException {
        final BulkRequest bulkRequest = new BulkRequest();
        list.forEach(document -> {
            final IndexRequest indexRequest = new IndexRequest(index);
            indexRequest.source(document, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        return client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 指定document _id
     */
    public static BulkResponse bulkByIds(RestHighLevelClient client, final String index,
                                         final Map<String, Map<String, Object>> documents) throws IOException {
        final BulkRequest bulkRequest = new BulkRequest();
        documents.forEach((k, v) -> {
            final IndexRequest indexRequest = new IndexRequest(index);
            indexRequest.source(v);
            indexRequest.id(k);
            bulkRequest.add(indexRequest);
        });
        return client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
}
