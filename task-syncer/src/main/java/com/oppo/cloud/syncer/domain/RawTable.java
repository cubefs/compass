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
 * Kafka同步数据表结构
 */
@Data
public class RawTable {

    /**
     * 当前数据
     */
    private List<Map<String, String>> data;
    /**
     * 数据库
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
     * 是否为ddl
     */
    private Boolean isDdl;
    /**
     * 数据库表字段类型, 如 {"id":"int(11)","name":"varchar(32)","age":"int(11)"}
     */
    private Map<String, String> mysqlType;
    /**
     * 变更旧字段及值
     */
    private List<Map<String, String>> old;
    /**
     * 主键
     */
    private List<String> pkNames;
    /**
     * sql
     */
    private String sql;
    /**
     * sql字段类型
     */
    private Map<String, Object> sqlType;
    /**
     * 变更数据表
     */
    private String table;
    /**
     * 时间戳
     */
    private Long ts;
    /**
     * 变更操作类型: INSERT, UPDATE, DELETE...
     */
    private String type;
}
