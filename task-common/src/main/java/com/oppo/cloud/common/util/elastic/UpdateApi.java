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

import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;
import java.util.Map;

/**
 * 更新文档API
 */
public class UpdateApi {

    public static UpdateResponse update(RestHighLevelClient client, final String index, final String id,
                                 final Map<String, Object> doucment) throws IOException {
        final UpdateRequest request = new UpdateRequest(index, id).doc(doucment);
        return client.update(request, RequestOptions.DEFAULT);
    }

    public UpdateResponse upsert(RestHighLevelClient client, final String index, final String id,
                                 final Map<String, Object> document) throws IOException {
        final UpdateRequest request = new UpdateRequest(index, id).doc(document).upsert(document);

        return client.update(request, RequestOptions.DEFAULT);
    }

    public UpdateResponse upsertJson(RestHighLevelClient client, final String index, final String id,
                                     String doc) throws IOException {
        final UpdateRequest request =
                new UpdateRequest(index, id).doc(doc, XContentType.JSON).upsert(doc, XContentType.JSON);
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        return client.update(request, RequestOptions.DEFAULT);
    }

}
