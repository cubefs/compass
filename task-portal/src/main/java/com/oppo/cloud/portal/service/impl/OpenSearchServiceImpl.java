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
import com.oppo.cloud.common.domain.opensearch.OpenSearchInfo;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.domain.task.IndicatorData;
import com.oppo.cloud.portal.service.OpenSearchService;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.index.reindex.UpdateByQueryRequest;
import org.opensearch.script.Script;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.histogram.*;
import org.opensearch.search.aggregations.metrics.ParsedSum;
import org.opensearch.search.aggregations.metrics.ParsedValueCount;
import org.opensearch.search.aggregations.metrics.SumAggregationBuilder;
import org.opensearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class OpenSearchServiceImpl implements OpenSearchService {

    @Resource(name = "opensearch")
    private RestHighLevelClient restHighLevelClient;

    /**
     * Find by SearchSourceBuilder
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
    public List<IndicatorData> findValueByDayBuckets(String dateHistogram, SearchSourceBuilder builder, String... indexes) throws Exception {
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
        ParsedDateHistogram terms = aggregations.get(dateHistogram);
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
     * Count by SearchSourceBuilder
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
     * Find by Class type and SearchSourceBuilder
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
                    if (value instanceof OpenSearchInfo) {
                        ((OpenSearchInfo) value).setIndex(hit.getIndex());
                        ((OpenSearchInfo) value).setDocId(hit.getId());
                    }
                    items.add(value);
                } catch (Exception e) {
                    log.error("parse json exception,: hit: {}, exception: {}", hit.getSourceAsString(), e.getMessage());
                    throw new Exception(String.format("parse json exception: %s", e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new Exception(String.format("Exception: %s", e.getMessage()));
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
     * Generate SearchBuilder
     */
    @Override
    public SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                                Map<String, SortOrder> sort,
                                                Map<String, Object> or) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        // query condition
        if (termQuery != null) {
            for (String key : termQuery.keySet()) {
                Object value = termQuery.get(key);
                if (value == null) {
                    // Query for null value
                    boolQuery.mustNot(QueryBuilders.existsQuery(key));
                } else if ("".equals(value)) {
                    // Does not match any valid string
                    boolQuery.mustNot(QueryBuilders.wildcardQuery(key, "*"));
                } else if (value instanceof java.util.List) {
                    // List query
                    boolQuery.filter(QueryBuilders.termsQuery(key, (List<Object>) value));
                } else if (value instanceof Object[]) {
                    //  List array
                    boolQuery.filter(QueryBuilders.termsQuery(key, (Object[]) value));
                } else {
                    // Single string query
                    boolQuery.filter(QueryBuilders.termQuery(key, value));
                }
            }
        }
        // or condition query[xx and (a=1 or c=2)]
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
        // range condition
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
    public UpdateResponse insertOrUpdate(String index, String id, Object document) throws Exception {
        String json = "";
        try {
            json = JSON.toJSONString(document);
        } catch (Exception e) {
            throw new Exception(String.format("insertOrUpDate writeValueAsString failed:%s", e.getMessage()));
        }
        UpdateResponse updateResponse;
        try {
            UpdateRequest request =
                    new UpdateRequest(index, id).doc(json, XContentType.JSON).upsert(json, XContentType.JSON);
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new Exception(String.format("insertOrUpDate update failed:%s", e.getMessage()));
        }
        return updateResponse;
    }

    /**
     * Update by BoolQueryBuilder
     */
    @Override
    public void updateByQuery(BoolQueryBuilder boolQueryBuilder, Script script, String... indexNames) throws Exception {
        UpdateByQueryRequest update = new UpdateByQueryRequest(indexNames);
        update.setQuery(boolQueryBuilder);
        update.setScript(script);
        restHighLevelClient.updateByQueryAsync(update, RequestOptions.DEFAULT, null);
    }

    /**
     * Aggregate field values by day
     */
    @Override
    public List<IndicatorData> sumAggregationByDay(SearchSourceBuilder builder, long start, long end, String index, String aggField, String field) throws Exception {
        String dateHistogram = String.format("date_%s", UUID.randomUUID().toString());
//        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("date")
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(dateHistogram)
                .field(aggField).calendarInterval(DateHistogramInterval.DAY);
        dateHistogramAggregationBuilder.timeZone(ZoneId.of("GMT+8"));
        dateHistogramAggregationBuilder.extendedBounds(new LongBounds(DateUtil.timestampToUTCStr(start),
                DateUtil.timestampToUTCStr(end)));

        SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum("value").field(field);
        dateHistogramAggregationBuilder.subAggregation(sumAggregationBuilder);
        builder.aggregation(dateHistogramAggregationBuilder).size(0);

        return findValueByDayBuckets(dateHistogram, builder, index + "-*");
    }

    /**
     *  Aggregate the count of document by day
     */
    @Override
    public List<IndicatorData> countDocByDay(SearchSourceBuilder builder, long start, long end, String index, String aggField) throws Exception {
        String dateHistogram = String.format("date_%s", UUID.randomUUID().toString());
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(dateHistogram)
                .field(aggField).calendarInterval(DateHistogramInterval.DAY);
        dateHistogramAggregationBuilder.timeZone(ZoneId.of("GMT+8"));
        dateHistogramAggregationBuilder.extendedBounds(new LongBounds(DateUtil.timestampToUTCStr(start),
                DateUtil.timestampToUTCStr(end)));

        ValueCountAggregationBuilder valueCountAggregationBuilder = AggregationBuilders.count("value").field("_id");
        dateHistogramAggregationBuilder.subAggregation(valueCountAggregationBuilder);

        builder.aggregation(dateHistogramAggregationBuilder).size(0);

        return findValueByDayBuckets(dateHistogram, builder, index + "-*");
    }

}
