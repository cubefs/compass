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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class OOMWarn extends IsAbnormal {

    @ApiModelProperty(value = "广播表列表")
    private Table<BoardCastTable> table = new Table<>();

    public OOMWarn() {
        LinkedHashMap<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("hiveTable", "广播表名称");
        titleMap.put("outputOfColumns", "过滤后输出行数");
        titleMap.put("memoryUsed", "占用内存大小");
        table.setTitles(titleMap);
    }

    @Data
    public static class BoardCastTable {

        @ApiModelProperty(value = "广播表名称")
        private String hiveTable;

        @ApiModelProperty(value = "过滤后输出行数")
        private String outputOfColumns;

        @ApiModelProperty(value = "占用内存大小")
        private String memoryUsed;
    }

}
