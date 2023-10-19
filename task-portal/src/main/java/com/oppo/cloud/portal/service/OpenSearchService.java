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

package com.oppo.cloud.portal.service;

import com.oppo.cloud.portal.domain.task.IndicatorData;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.script.Script;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;

import java.util.List;
import java.util.Map;

/**
 * OpenSearchService
 */
public interface OpenSearchService {
    /**
     * Find by SearchSourceBuilder
     */
    SearchHits find(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * Query aggregated data by condition
     */
    Aggregations findRawAggregations(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * Query by bucketing by day
     */
    List<IndicatorData> findValueByDayBuckets(String bucket, SearchSourceBuilder builder, String... indexes) throws Exception;

    <T> List<T> find(Class<T> itemType, SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * Find by termQuery
     */
    <T> List<T> find(Class<T> itemType, Map<String, Object> termQueryConditions, String... indexes) throws Exception;

    /**
     * Count by SearchSourceBuilder
     */
    Long count(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * Generate SearchSourceBuilder
     */
    SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                         Map<String, SortOrder> sort,
                                         Map<String, Object> or);

    /**
     * Insert or update document
     */
    UpdateResponse insertOrUpdate(String index, String id, Object document) throws Exception;

    /**
     * Update by query
     */
    void updateByQuery(BoolQueryBuilder boolQueryBuilder, Script script, String... indexNames) throws Exception;

    /**
     * Aggregate field values by day
     */
    List<IndicatorData> sumAggregationByDay(SearchSourceBuilder builder, long start, long end, String index, String aggField, String filed) throws Exception;

    /**
     * Aggregate the count of document by day
     */
    List<IndicatorData> countDocByDay(SearchSourceBuilder builder, long start, long end, String index, String aggField) throws Exception;
}
