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

package com.oppo.cloud.application.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class HDFSUtilTest {

    @Resource
    private ObjectMapper objectMapper;

    @Test
    void getNameNode() throws JsonProcessingException {
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("test", "test");
        String aa = objectMapper.writeValueAsString(rawData);

        Map<String, Object> objectMap = objectMapper.readValue(aa, new TypeReference<Map<String, Object>>() {
        });
        System.out.println(objectMap);
    }
}
