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

package com.oppo.cloud.common.elasticsearch;

import com.oppo.cloud.common.domain.elasticsearch.JobAnalysisMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class JobAnalysisMappingTest {

    @Test
    public void build() {
        Map<String, Object> mapping = JobAnalysisMapping.build();
        log.info("{}", mapping);
        Assertions.assertNotNull(mapping.get("users"));
        Assertions.assertNotNull(mapping.get("projectName"));
        Assertions.assertNotNull(mapping.get("projectId"));
        Assertions.assertNotNull(mapping.get("flowName"));
        Assertions.assertNotNull(mapping.get("flowId"));
        Assertions.assertNotNull(mapping.get("taskName"));
        Assertions.assertNotNull(mapping.get("taskId"));
        Assertions.assertNotNull(mapping.get("executionDate"));
        Assertions.assertNotNull(mapping.get("startTime"));
        Assertions.assertNotNull(mapping.get("endTime"));
        Assertions.assertNotNull(mapping.get("duration"));
        Assertions.assertNotNull(mapping.get("taskState"));
        Assertions.assertNotNull(mapping.get("memorySeconds"));
        Assertions.assertNotNull(mapping.get("vcoreSeconds"));
        Assertions.assertNotNull(mapping.get("taskType"));
        Assertions.assertNotNull(mapping.get("retryTimes"));
        Assertions.assertNotNull(mapping.get("categories"));
        Assertions.assertNotNull(mapping.get("normalRange"));
        Assertions.assertNotNull(mapping.get("successDate"));
        Assertions.assertNotNull(mapping.get("successDays"));
        Assertions.assertNotNull(mapping.get("memory"));
        Assertions.assertNotNull(mapping.get("memoryRatio"));
        Assertions.assertNotNull(mapping.get("deleted"));
        Assertions.assertNotNull(mapping.get("taskStatus"));
        Assertions.assertNotNull(mapping.get("createTime"));
        Assertions.assertNotNull(mapping.get("updateTime"));
    }
}
