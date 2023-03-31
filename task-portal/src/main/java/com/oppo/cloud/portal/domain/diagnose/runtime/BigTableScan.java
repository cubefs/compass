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

package com.oppo.cloud.portal.domain.diagnose.runtime;

import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;


@Data
@ApiModel("大表扫描")
public class BigTableScan extends IsAbnormal {

    @ApiModelProperty(value = "大表扫描表单数据")
    private Table<TaskInfo> table = new Table<>();

    public BigTableScan() {
        LinkedHashMap<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("hiveTable", "扫描的hive表名称");
        titleMap.put("columns", "扫描行数");
        titleMap.put("threshold", "阈值");
        table.setTitles(titleMap);
    }

    @Data
    public static class TaskInfo {

        @ApiModelProperty(value = "扫描的hive表名称")
        private String hiveTable;

        @ApiModelProperty(value = "扫描行数")
        private String columns;

        @ApiModelProperty(value = "阈值")
        private String threshold;
    }
}
