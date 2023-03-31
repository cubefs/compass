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

package com.oppo.cloud.detect.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.constant.YarnAppFinalStatus;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.elasticsearch.EsInfo;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.detect.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ElasticSearchServiceImp implements ElasticSearchService {

    @Value("${custom.elasticsearch.yarn-app-index}")
    private String yarnAppIndex;

    @Value("${custom.elasticsearch.spark-app-index}")
    private String sparkAppIndex;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    /**
     * 查询ES数据
     */
    @Override
    public SearchHits search(String indexNames, HashMap<String, Object> termQuery,
                             HashMap<String, Object> lte, HashMap<String, Object> gte,
                             HashMap<String, SortOrder> sort, Integer size) throws Exception {
        return null;
    }

    /**
     * 构建通用查询条件
     */
    @Override
    public SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                                Map<String, SortOrder> sort,
                                                Map<String, Object> or) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        // 查询条件
        for (String key : termQuery.keySet()) {
            Object value = termQuery.get(key);
            if (value == null) {
                // null值查询
                boolQuery.mustNot(QueryBuilders.existsQuery(key));
            } else if ("".equals(value)) {
                // 不匹配任何有效字符串
                boolQuery.mustNot(QueryBuilders.wildcardQuery(key, "*"));
            } else if (value instanceof java.util.List) {
                // 列表查询
                boolQuery.filter(QueryBuilders.termsQuery(key, (List<String>) value));
            } else {
                // 单字符串查询
                boolQuery.filter(QueryBuilders.termsQuery(key, value));
            }
        }
        // or条件查询[xx and (a=1 or c=2)]
        if (or != null) {
            BoolQueryBuilder orQuery = new BoolQueryBuilder();
            for (String key : or.keySet()) {
                Object value = or.get(key);
                if (value != null) {
                    orQuery.should(QueryBuilders.termQuery(key, value));
                }
            }
            boolQuery.must(orQuery);
        }
        // 范围查询
        if (rangeConditions != null) {
            for (String key : rangeConditions.keySet()) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(key);
                Object[] queryValue = rangeConditions.get(key);
                if (queryValue[0] != null) {
                    rangeQueryBuilder.gte(queryValue[0]);
                }
                if (queryValue[1] != null) {
                    rangeQueryBuilder.lte(queryValue[1]);
                }
                boolQuery.filter(rangeQueryBuilder);
            }
        }
        // sort
        if (sort != null) {
            for (String key : sort.keySet()) {
                builder.sort(key, sort.get(key));
            }
        }
        builder.query(boolQuery);
        return builder;
    }

    /**
     * 根据查询条件对象和索引查询原始数据
     *
     * @param builder 查询条件
     * @param indexes 查询索引
     * @return
     * @throws Exception
     */
    @Override
    public SearchHits find(SearchSourceBuilder builder, String... indexes) throws Exception {
        SearchRequest searchRequest = new SearchRequest().indices(indexes).source(builder);
        Long startTime = System.currentTimeMillis();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Long endTime = System.currentTimeMillis();
        log.info("indexes:{}, duration:{} ,condition:{}", indexes, (endTime - startTime) / 1000, builder.toString());
        return searchResponse.getHits();
    }

    /**
     * 查询YarnApp数据
     */
    @Override
    public YarnApp searchYarnApp(String applicationId) throws Exception {
        YarnApp yarnApp = null;
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("id.keyword", applicationId);
        SearchSourceBuilder searchSourceBuilder = this.genSearchBuilder(termQuery, null, null, null);
        SearchHits searchHits = this.find(searchSourceBuilder, yarnAppIndex + "-*");
        if (searchHits.getHits().length == 0) {
            throw new Exception(String.format("can not find this appId from yarnApp, appId:%s", applicationId));
        }
        for (SearchHit hit : searchHits) {
            yarnApp = JSON.parseObject(hit.getSourceAsString(), YarnApp.class);
        }
        if (yarnApp == null) {
            throw new Exception(String.format("yarnApp is null, appId:%s", applicationId));
        }
        if (yarnApp.getFinalStatus().equals(YarnAppFinalStatus.SUCCEEDED.toString()) ||
                yarnApp.getFinalStatus().equals(YarnAppFinalStatus.FAILED.toString()) ||
                yarnApp.getFinalStatus().equals(YarnAppFinalStatus.KILLED.toString())) {
            return yarnApp;
        }
        throw new Exception(String.format("yarnApp state:%s, finalStatus:%s, appId:%s", yarnApp.getState(),
                yarnApp.getFinalStatus(), applicationId));
    }


    /**
     * 查询SparkApp数据
     */
    @Override
    public SparkApp searchSparkApp(String applicationId) throws Exception {
        SparkApp sparkApp = null;
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("appId.keyword", applicationId);
        SearchSourceBuilder searchSourceBuilder = this.genSearchBuilder(termQuery, null, null, null);
        SearchHits searchHits = this.find(searchSourceBuilder, sparkAppIndex + "-*");
        if (searchHits.getHits().length == 0) {
            throw new Exception(String.format("can not find this appId from sparkAppIndex, appId:%s", applicationId));
        }
        for (SearchHit hit : searchHits) {
            sparkApp = JSON.parseObject(hit.getSourceAsString(), SparkApp.class);
            break;
        }
        return sparkApp;
    }

    @Override
    public UpdateResponse insertOrUpDateEs(String index, String id, Object document) throws Exception {
        String json = "";
        try {
            json = JSON.toJSONString(document);
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

    /**
     * 根据查询条件和索引查询数据
     */
    @Override
    public <T extends EsInfo> List<T> find(Class<T> itemType, SearchSourceBuilder builder,
                                           String... indexes) throws Exception {
        List<T> items = new ArrayList<>();
        SearchHits hits;
        try {
            hits = this.find(builder, indexes);
            for (SearchHit hit : hits) {
                try {
                    T value = JSON.parseObject(hit.getSourceAsString(), itemType, DateUtil.getUTCContext());
                    value.setDocId(hit.getId());
                    value.setIndex(hit.getIndex());
                    items.add(value);
                } catch (Exception e) {
                    log.error("解析Json数据异常，原始数据: {}, 异常: {}", hit.getSourceAsString(), e.getMessage());
                    throw new Exception(String.format("解析Json数据异常: %s", e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("查询Elasticsearch异常: {}", e.getMessage());
            throw new Exception(String.format("查询Elasticsearch异常: %s", e.getMessage()));
        }
        return items;
    }
}
