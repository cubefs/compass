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

package com.oppo.cloud.common.domain.opensearch;

import com.oppo.cloud.common.util.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class LogSummary extends OpenSearchInfo {

    @ApiModelProperty(value = "applicationId名称")
    private String applicationId;

    @ApiModelProperty(value = "日志类型")
    private String logType;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "执行周期")
    private Date executionDate;

    @ApiModelProperty(value = "任务重试次数")
    private Integer retryTimes;

    @ApiModelProperty(value = "日志异常类型")
    private String action;

    @ApiModelProperty(value = "日志解析步骤")
    private Integer step;

    @ApiModelProperty(value = "正则匹配的变量名")
    private List<String> groupNames;

    @ApiModelProperty(value = "原始日志")
    private String rawLog;

    @ApiModelProperty(value = "原始日志路径")
    private String logPath;

    @ApiModelProperty(value = "正则匹配变量值")
    private Map<String, String> groupData;

    @ApiModelProperty(value = "日志时间")
    private Integer logTimestamp;

    public Map<String, Object> genDoc() throws Exception {
        Map<String, Object> res = new HashMap<>();
        Field[] fileds = this.getClass().getDeclaredFields();
        for (Field field : fileds) {
            String key = field.getName();
            String method = key.substring(0, 1).toUpperCase() + key.substring(1);
            Method getMethod = this.getClass().getMethod("get" + method);
            switch (field.getName()) {
                case "docId":
                    break;
                case "executionDate":
                    Date value = (Date) getMethod.invoke(this);
                    if (value != null) {
                        res.put(key, DateUtil.timestampToUTCDate(value.getTime()));
                    }
                    break;
                default:
                    res.put(key, getMethod.invoke(this));
            }
        }
        return res;
    }
}
