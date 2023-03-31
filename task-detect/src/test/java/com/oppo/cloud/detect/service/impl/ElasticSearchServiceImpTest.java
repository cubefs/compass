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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.cluster.spark.SparkApp;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.detect.service.ElasticSearchService;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


@SpringBootTest
class ElasticSearchServiceImpTest {

    @MockBean
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @SpyBean
    private ElasticSearchService elasticSearchServiceMock;

    public SearchHits getEsMsgObject(String msgJson) {
        BytesReference source = new BytesArray(msgJson);
        SearchHit hit = new SearchHit(1);
        hit.sourceRef(source);
        TotalHits totalHits = new TotalHits(5, TotalHits.Relation.EQUAL_TO);
        return new SearchHits(new SearchHit[]{hit}, totalHits, 10);
    }

    @Test
    void search() {
        HashMap<String, Object> termQuery = new HashMap<>();
        HashMap<String, Object> lte = new HashMap<>();
        HashMap<String, Object> gte = new HashMap<>();
        HashMap<String, SortOrder> sort = new HashMap<>();
        // in查询
        termQuery.put("logType", Arrays.asList("scheduler", "event", "driver", "executor"));
        // 空字符串查询
        termQuery.put("applicationId", "");
        // 正常字符串查询
        termQuery.put("taskName", "test_task");
        // null值查询
        termQuery.put("projectName", null);
        // 小于查询
        lte.put("step", 10);
        // 大于查询
        gte.put("ctime", 10);
        // 排序
        sort.put("timestamp", SortOrder.ASC);
        try {
            elasticSearchService.search("log-summary", termQuery, lte, gte, sort, 10000);
        } catch (Exception e) {
            Assert.assertNull(e.getMessage());
        }
    }

    @Test
    void searchYarnApp() {
        YarnApp yarnApp = new YarnApp();
        yarnApp.setStartedTime(new Date().getTime() / 1000);
        yarnApp.setFinishedTime(new Date().getTime() / 1000);
        yarnApp.setElapsedTime(100l);
        yarnApp.setClusterName("clusterName_test");
        yarnApp.setApplicationType("SPARK");
        yarnApp.setQueue("queue_test");
        yarnApp.setDiagnostics("kill by user");
        yarnApp.setUser("hive");
        yarnApp.setVcoreSeconds(1000);
        yarnApp.setMemorySeconds(1000);
        yarnApp.setIp("10.11.10");
        try {
            String mockMsg = objectMapper.writeValueAsString(yarnApp);
            SearchHits searchHits = getEsMsgObject(mockMsg);
            Mockito.doReturn(searchHits).when(elasticSearchServiceMock).search(Mockito.any(),
                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
            YarnApp yarnAppRes = elasticSearchServiceMock.searchYarnApp("appId");
            System.out.println(yarnAppRes);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test
    void searchSparkApp() {
        SparkApp sparkApp = new SparkApp();
        sparkApp.setEventLogDirectory("hdfs://test");
        try {
            String mockMsg = objectMapper.writeValueAsString(sparkApp);
            SearchHits searchHits = getEsMsgObject(mockMsg);
            Mockito.doReturn(searchHits).when(elasticSearchServiceMock).search(Mockito.any(),
                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
            SparkApp sparkAppRes = elasticSearchServiceMock.searchSparkApp("appId");
            System.out.println(sparkAppRes);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
