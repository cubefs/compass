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

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ReindexRequest;

import java.io.IOException;

/**
 * 索引重命名API
 */
public class ReindexApi {

    public BulkByScrollResponse reindex(RestHighLevelClient client, final String[] sourceIndices,
                                        final String destIndex) throws IOException {
        final ReindexRequest request = new ReindexRequest()
                .setSourceIndices(sourceIndices)
                .setDestIndex(destIndex);
        return client.reindex(request, RequestOptions.DEFAULT);
    }
}
