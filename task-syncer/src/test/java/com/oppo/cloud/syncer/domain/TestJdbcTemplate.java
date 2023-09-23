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

package com.oppo.cloud.syncer.domain;

import com.oppo.cloud.model.TaskInstance;
import com.oppo.cloud.syncer.dao.TaskInstanceExtendMapper;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@SpringBootTest
@ComponentScan(basePackages = "com.oppo.cloud")
public class TestJdbcTemplate {

    @Autowired
    @Qualifier("diagnoseJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testQuery() {
        String sql = "select 1";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        System.out.println(result);
        Assertions.assertTrue(result.get("1") != null, "jdbc query result not be empty!");
    }

}
