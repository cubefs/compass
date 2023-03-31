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
import com.oppo.cloud.common.domain.elasticsearch.EsInfo;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ElasticSearch查询service
 */
public interface ElasticSearchService {

    /**
     * 查询ES数据
     */
    @Deprecated
    SearchHits search(String indexNames, HashMap<String, Object> termQueryConditions,
                      HashMap<String, Object> lteConditions, HashMap<String, Object> gteConditions,
                      HashMap<String, SortOrder> sortConditions, Integer size) throws Exception;

    /**
     * 构建通用查询条件
     */
    SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                         Map<String, SortOrder> sort,
                                         Map<String, Object> or);

    /**
     * 按条件查询记录, 分页查询
     */
    SearchHits find(SearchSourceBuilder builder, String... indexes) throws Exception;

    /**
     * 查询YarnApp数据
     */
    YarnApp searchYarnApp(String applicationId) throws Exception;

    /**
     * 查询SparkApp数据
     */
    SparkApp searchSparkApp(String applicationId) throws Exception;

    /**
     * 插入或更新Es数据库
     */
    UpdateResponse insertOrUpDateEs(String index, String id, Object document) throws Exception;

    /**
     * 查询数据
     */
    <T extends EsInfo> List<T> find(Class<T> itemType, SearchSourceBuilder builder, String... indexes) throws Exception;
}
