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

package com.oppo.cloud.detect.service;

import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.opensearch.OpenSearchInfo;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;

/**
 * OpenSearchService
 */
public interface OpenSearchService {


    /**
     * Construct a general query condition.
     */
    SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                         Map<String, SortOrder> sort,
                                         Map<String, Object> or);

    /**
     * Query records by condition, and query by page.
     */
    SearchHits find(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * Query YarnApp data.
     */
    YarnApp searchYarnApp(String applicationId) throws Exception;

    /**
     * Query SparkApp data.
     */
    SparkApp searchSparkApp(String applicationId) throws Exception;

    /**
     * Insert or update.
     */
    UpdateResponse insertOrUpDate(String index, String id, Object document) throws Exception;

    /**
     * Query data.
     */
    <T extends OpenSearchInfo> List<T> find(Class<T> itemType, SearchSourceBuilder builder, String... indexes) throws Exception;
}
