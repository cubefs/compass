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

package com.oppo.cloud.common.util.opensearch;

import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.*;
import org.opensearch.common.settings.Settings;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Create dynamic template API
 */
public class MappingApi {

    // 同步创建mapping
    public AcknowledgedResponse put(RestHighLevelClient client, final String index,
                                    final Map<String, Object> mapping) throws IOException {
        PutMappingRequest request = new PutMappingRequest(index).source(mapping);
        return client.indices().putMapping(request, RequestOptions.DEFAULT);
    }

    /**
     * Create mapping
     */
    public CreateIndexResponse create(RestHighLevelClient client, final String index, final Map<String, Object> mapping,
                                      int numberOfShards, int numberOfReplicas) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index).settings(Settings.builder()
                .put("index.number_of_shards", numberOfShards)
                .put("index.number_of_replicas", numberOfReplicas));
        request.mapping(mapping);
        return client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * Create template mapping
     */
    public AcknowledgedResponse putTemplate(RestHighLevelClient client, final String template, String[] patterns,
                                            final Map<String, Object> mapping, int numberOfShards,
                                            int numberOfReplicas) throws IOException {
        PutIndexTemplateRequest request = new PutIndexTemplateRequest(template).patterns(Arrays.asList(patterns))
                .settings(Settings.builder()
                        .put("index.number_of_shards", numberOfShards)
                        .put("index.number_of_replicas", numberOfReplicas));
        request.mapping(mapping);
        return client.indices().putTemplate(request, RequestOptions.DEFAULT);
    }

    /**
     * Delete template by name
     */
    public AcknowledgedResponse delete(RestHighLevelClient client, final String template) throws IOException {
        DeleteIndexTemplateRequest request = new DeleteIndexTemplateRequest();
        request.name(template);
        return client.indices().deleteTemplate(request, RequestOptions.DEFAULT);
    }

    /**
     * Check if template exists
     */
    public boolean existsTemplate(RestHighLevelClient client, final String template) throws IOException {
        IndexTemplatesExistRequest request = new IndexTemplatesExistRequest(template);
        return client.indices().existsTemplate(request, RequestOptions.DEFAULT);
    }
}
