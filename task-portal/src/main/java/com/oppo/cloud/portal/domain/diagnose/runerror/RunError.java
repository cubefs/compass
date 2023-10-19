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

package com.oppo.cloud.portal.domain.diagnose.runerror;

import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Table;
import com.oppo.cloud.portal.domain.log.LogInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@ApiModel("RunError information")
public class RunError extends IsAbnormal {

    @ApiModelProperty("abnormal log information")
    private Table<LogInfo> table = new Table<>();

    public RunError() {
        LinkedHashMap<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("logType", "日志类型");
        titleMap.put("event", "事件描述");
        titleMap.put("logTime", "时间");
        titleMap.put("logContent", "关键日志");
        titleMap.put("advice", "诊断建议");
        this.table.setTitles(titleMap);
    }
}
