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

import com.oppo.cloud.syncer.config.DataSourceConfig;
import com.oppo.cloud.syncer.domain.Mapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.Resource;

@SpringBootConfiguration
@SpringBootTest
@ComponentScan(basePackages = "com.oppo.cloud")
public class TestDatasourceConfig {

    @Resource
    private DataSourceConfig dataSourceConfig;

    @Test
    public void testDatabase() {
        Assertions.assertTrue(dataSourceConfig.getMappings().size() > 0, "mapping should not be zero");

        for (Mapping mapping : dataSourceConfig.getMappings()) {
            Assertions.assertTrue(mapping.getSchema() != null, "schema shoud not be empty");
            Assertions.assertTrue(mapping.getTable() != null, "table should not be null");
            Assertions.assertTrue(mapping.getTargetTable() != null, "target table should not be null");
            Assertions.assertTrue(mapping.getColumnMapping() != null, "columnMapping should not be null");
        }
    }
}
