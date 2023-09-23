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

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.SpeculativeTaskAbnormal;
import com.oppo.cloud.portal.service.OpenSearchService;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
class TaskAppServiceImplTest {

    @Autowired
    OpenSearchService openSearchService;


    @Test
    void generateReport() {
        try {
            HashMap<String, Object> termQueryConditions = new HashMap<>(2);
            termQueryConditions.put("applicationId.keyword", "appid");
            List<DetectorStorage> detectorStorageList =
                    openSearchService.find(DetectorStorage.class, termQueryConditions, "detector-log-2022-09-27");
            if (detectorStorageList.size() != 0) {
                DetectorStorage detectorStorage = detectorStorageList.get(0);
                for (DetectorResult result : detectorStorage.getDataList()) {
                    if (result.getAppCategory().equals("dataSkew")) {
                        List<JSONObject> temp = (List<JSONObject>) result.getData();
                        System.out.println(temp);
                        temp.forEach(data -> {
                            SpeculativeTaskAbnormal taskAbnormal = data.toJavaObject(SpeculativeTaskAbnormal.class);
                            System.out.println(taskAbnormal);
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUpdateDetectorEs() throws Exception {
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId", "application_1662709492856_0124");
        SearchSourceBuilder searchSourceBuilder = openSearchService.genSearchBuilder(termQuery, null, null, null);
        List<DetectorStorage> detectionStorageList =
                openSearchService.find(DetectorStorage.class, searchSourceBuilder, "compass-detector-app" + "-*");
        for (DetectorStorage detectorStorage : detectionStorageList) {
            detectorStorage.setApplicationId("application_1662709492856_1124");
            openSearchService.insertOrUpDate("compass-detector-app-2022-10-24",
                    "bacf1ef9-daf3-4c4d-806d-503546cecff2", detectorStorage);
        }
    }
}
