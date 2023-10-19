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


@Data
public class GlobalSort extends IsAbnormal {

    @ApiModelProperty(value = "table information")
    private Table<GlobalSortTable> table = new Table<>();

    public GlobalSort() {
        LinkedHashMap<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("jobId", "JobId");
        titleMap.put("stageId", "StageId");
        titleMap.put("taskNum", "任务个数");
        titleMap.put("dataOfColumns", "处理数据量(行)");
        titleMap.put("duration", "执行耗时");
        table.setTitles(titleMap);
    }

    @Data
    public static class GlobalSortTable {

        @ApiModelProperty(value = "JobId")
        private String jobId;

        @ApiModelProperty(value = "stageId")
        private String stageId;

        @ApiModelProperty(value = "task number")
        private String taskNum;

        @ApiModelProperty(value = "task size")
        private String dataOfColumns;

        @ApiModelProperty(value = "duration")
        private String duration;
    }
}
