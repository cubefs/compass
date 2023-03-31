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

import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 批量获取文档API
 */
public class MultiGetApi {

    public MultiGetResponse multiGet(RestHighLevelClient client,
                                     List<Map<String, String>> documents) throws IOException {
        final MultiGetRequest request = new MultiGetRequest();
        documents.forEach(document -> request.add(new MultiGetRequest.Item(
                document.get("index"),
                document.get("id"))));
        return client.mget(request, RequestOptions.DEFAULT);
    }
}
