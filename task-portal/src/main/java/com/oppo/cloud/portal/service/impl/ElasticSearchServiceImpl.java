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

package com.oppo.cloud.portal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.elasticsearch.EsInfo;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.domain.task.IndicatorData;
import com.oppo.cloud.portal.domain.task.JobsRequest;
import com.oppo.cloud.portal.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.metrics.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Service
@Slf4j
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

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

    @Override
    public Aggregations findRawAggregations(SearchSourceBuilder builder, String... indexes) throws Exception {
        SearchRequest searchRequest = new SearchRequest().indices(indexes).source(builder);
        Long startTime = System.currentTimeMillis();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Long endTime = System.currentTimeMillis();
        log.info("indexes:{}, duration:{} ,condition:{}", indexes, (endTime - startTime) / 1000, builder.toString());
        return searchResponse.getAggregations();
    }

    @Override
    public List<IndicatorData> findValueByDayBuckets(SearchSourceBuilder builder, String... indexes) throws Exception {
        List<IndicatorData> indicatorDataList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest().indices(indexes).source(builder);
        Long startTime = System.currentTimeMillis();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Long endTime = System.currentTimeMillis();
        log.info("indexes:{}, duration:{}, condition:{}", indexes, (endTime - startTime) / 1000, builder.toString());
        Aggregations aggregations = searchResponse.getAggregations();
        if (aggregations == null) {
            return null;
        }
        ParsedDateHistogram terms = aggregations.get("date");
        List<? extends Histogram.Bucket> buckets = terms.getBuckets();
        for (Histogram.Bucket bucket : buckets) {
            IndicatorData indicatorData = new IndicatorData();
            String dateStr = bucket.getKeyAsString();
            Object parsedValue = bucket.getAggregations().get("value");
            double num = 0;
            if (parsedValue instanceof ParsedSum) {
                num = ((ParsedSum) parsedValue).getValue();
            } else {
                num = ((ParsedValueCount) parsedValue).getValue();
            }
            indicatorData.setDate(dateStr);
            indicatorData.setCount(num);
            indicatorDataList.add(indicatorData);
        }
        return indicatorDataList;
    }

    /**
     * 根据条件查询记录条数
     *
     * @param builder
     * @param indexes
     * @return
     * @throws Exception
     */
    @Override
    public Long count(SearchSourceBuilder builder, String... indexes) throws Exception {
        CountRequest request = new CountRequest();
        request.indices(indexes);
        request.source(builder);
        log.info("indexes:{} ,condition:{}", indexes, builder.toString());
        CountResponse response = restHighLevelClient.count(request, RequestOptions.DEFAULT);
        return response.getCount();
    }

    /**
     * 根据查询条件和索引查询数据
     *
     * @param itemType 查询对象类型
     * @param builder  查询条件
     * @param indexes  查询索引
     * @param <T>      数据类型
     * @return
     * @throws Exception
     */
    @Override
    public <T> List<T> find(Class<T> itemType, SearchSourceBuilder builder, String... indexes) throws Exception {
        List<T> items = new ArrayList<>();
        SearchHits hits;
        try {
            hits = this.find(builder, indexes);
            for (SearchHit hit : hits) {
                try {
                    T value = JSON.parseObject(hit.getSourceAsString(), itemType, DateUtil.getUTCContext());
                    if (value instanceof EsInfo) {
                        ((EsInfo) value).setIndex(hit.getIndex());
                        ((EsInfo) value).setDocId(hit.getId());
                    }
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

    @Override
    public <T> List<T> find(Class<T> itemType, Map<String, Object> termQueryConditions,
                            String... indexes) throws Exception {
        SearchSourceBuilder searchSourceBuilder = this.genSearchBuilder(termQueryConditions, null, null, null);
        return this.find(itemType, searchSourceBuilder, indexes);
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
        if (termQuery != null) {
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
                    boolQuery.filter(QueryBuilders.termQuery(key, value));
                }
            }
        }
        // or条件查询[xx and (a=1 or c=2)]
        if (or != null) {
            BoolQueryBuilder boolOrQueryBuilder = new BoolQueryBuilder();
            for (String key : or.keySet()) {
                Object queryValue = or.get(key);
                if (queryValue != null) {
                    if (queryValue instanceof java.util.List) {
                        boolOrQueryBuilder.should(QueryBuilders.termsQuery(key, (List<String>) queryValue));
                    } else {
                        boolOrQueryBuilder.should(QueryBuilders.termQuery(key, queryValue));
                    }
                }
            }
            boolQuery.must(boolOrQueryBuilder);
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
     * 按查询条件更新数据
     */
    @Override
    public void updateByQuery(BoolQueryBuilder boolQueryBuilder, Script script, String... indexNames) throws Exception {
        UpdateByQueryRequest update = new UpdateByQueryRequest(indexNames);
        update.setQuery(boolQueryBuilder);
        update.setScript(script);
        restHighLevelClient.updateByQueryAsync(update, RequestOptions.DEFAULT, null);
    }

    /**
     * 按天聚合字段值和
     */
    @Override
    public List<IndicatorData> sumAggregationByDay(SearchSourceBuilder builder, long start, long end, String index, String filed) throws Exception {

        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("date")
                .field("executionDate").calendarInterval(DateHistogramInterval.DAY);
        dateHistogramAggregationBuilder.timeZone(ZoneId.of("GMT+8"));
        dateHistogramAggregationBuilder.extendedBounds(new LongBounds(DateUtil.timestampToUTCStr(start),
                DateUtil.timestampToUTCStr(end)));

        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("value").field(filed);
        dateHistogramAggregationBuilder.subAggregation(sumAggregationBuilder);
        builder.aggregation(dateHistogramAggregationBuilder).size(0);

        return findValueByDayBuckets(builder, index + "-*");
    }

    /**
     * 按天统计数量聚合
     */
    @Override
    public List<IndicatorData> countDocByDay(SearchSourceBuilder builder, long start, long end, String index) throws Exception {

        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("date")
                .field("executionDate").calendarInterval(DateHistogramInterval.DAY);
        dateHistogramAggregationBuilder.timeZone(ZoneId.of("GMT+8"));
        dateHistogramAggregationBuilder.extendedBounds(new LongBounds(DateUtil.timestampToUTCStr(start),
                DateUtil.timestampToUTCStr(end)));

        ValueCountAggregationBuilder valueCountAggregationBuilder = AggregationBuilders.count("value").field("_id");
        dateHistogramAggregationBuilder.subAggregation(valueCountAggregationBuilder);

        builder.aggregation(dateHistogramAggregationBuilder).size(0);

        return findValueByDayBuckets(builder, index + "-*");
    }

}
