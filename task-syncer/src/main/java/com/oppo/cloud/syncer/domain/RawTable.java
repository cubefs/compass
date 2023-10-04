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

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Kafka synchronizes data table
 */
@Data
public class RawTable {

    /**
     * current data
     */
    private List<Map<String, String>> data;
    /**
     * database
     */
    private String database;
    /**
     * es
     */
    private Long es;
    /**
     * id
     */
    private Long id;
    /**
     * is ddl
     */
    private Boolean isDdl;
    /**
     * Database table field type
     * For example, {"id":"int(11)","name":"varchar(32)","age":"int(11)"}
     */
    private Map<String, String> mysqlType;
    /**
     * Change old fields and values
     */
    private List<Map<String, String>> old;
    /**
     * Primary key
     */
    private List<String> pkNames;
    /**
     * sql
     */
    private String sql;
    /**
     * sql Field Type
     */
    private Map<String, Object> sqlType;
    /**
     * Change data table
     */
    private String table;
    /**
     * Timestamp
     */
    private Long ts;
    /**
     * Operation: INSERT, UPDATE, DELETE...
     */
    private String type;
}
