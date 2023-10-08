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

package com.oppo.cloud.syncer.service.impl;

import com.oppo.cloud.syncer.domain.ColumnDep;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;
import com.oppo.cloud.syncer.util.DataUtil;
import com.oppo.cloud.syncer.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * public abstract service
 */
@Slf4j
abstract public class CommonService {

    /**
     * Data storage: insert, update
     */
    abstract void dataSave(Map<String, String> data, Mapping mapping, String action);

    /**
     * Data mapping
     */
    public void dataMapping(JdbcTemplate jdbcTemplate, RawTable rawTable, Mapping mapping, String action) {
        if (DataUtil.isEmpty(rawTable.getData())) {
            return;
        }

        Map<String, String> columnMapping = mapping.getColumnMapping();
        List<Map<String, String>> datas = DataUtil.mapData(rawTable.getData(), columnMapping);

        // value mapping
        DataUtil.mapColumnValue(datas, mapping.getColumnValueMapping());

        // Add constant column
        DataUtil.constantColumnValue(datas, mapping.getConstantColumn());

        ColumnDep columnDep = mapping.getColumnDep();

        for (Map<String, String> data : datas) {
            if (columnDep != null) {
                for (String query : columnDep.getQueries()) {
                    query = StringUtil.replaceParams(query, data);
                    Map<String, Object> result = null;
                    // possible delay
                    for (int i = 0; i < 3; i++) {
                        try {
                            result = jdbcTemplate.queryForMap(query);
                            break;
                        } catch (Exception e) {
                            log.warn("table: {},queryForMap: {},", mapping.getTargetTable(), query, e);
                            try {
                                TimeUnit.MILLISECONDS.sleep(300);
                            } catch (Exception ee) {

                            }
                        }
                    }
                    if (result == null) {
                        log.warn("query: {}, result is null!", query);
                        continue;
                    }
                    log.info("table: {},query:{}, result: {}", mapping.getTargetTable(), query, result);
                    for (String key : result.keySet()) {
                        Object v = result.get(key);
                        if (v == null) {
                            continue;
                        }
                        if (v instanceof LocalDateTime) {
                            v = DataUtil.formatDateObject(v);
                        }
                        data.put(key, v.toString());
                    }
                }
            } else {
                log.info("table: " + mapping.getTargetTable() + "; columnDep is null ");
            }
            log.info("dataMapping table:{}, data: {}", mapping.getTargetTable(), data.toString());

            dataSave(data, mapping, action);
        }
    }
}
