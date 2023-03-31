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
 * 表修改规则
 */
@Data
public class Mapping {

    /**
     * 同步数据库
     */
    private String schema;
    /**
     * 同步源表名
     */
    private String table;
    /**
     * 同步目标表名
     */
    private String targetTable;
    /**
     * 字段转为新字段映射
     */
    private Map<String, String> columnMapping;
    /**
     * 源字段值到目标字段值映射
     */
    private Map<String, List<ValueMapping>> columnValueMapping;
    /**
     * 目标值依赖其他表
     */
    private ColumnDep columnDep;
    /**
     * 同步映射后的数据写入kafka
     */
    private String writeKafkaTopic;
    /**
     * 源字段值到目标字段值映射
     */
    private Map<String, String> constantColumn;
}
