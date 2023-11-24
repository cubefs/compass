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
import java.util.*;


@Data
public class LogSummary extends OpenSearchInfo {

    @ApiModelProperty(value = "applicationId")
    private String applicationId;

    @ApiModelProperty(value = "log type")
    private String logType;

    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "execution date")
    private Date executionDate;

    @ApiModelProperty(value = "retry times")
    private Integer retryTimes;

    @ApiModelProperty(value = "action type")
    private String action;

    @ApiModelProperty(value = "step")
    private Integer step;

    @ApiModelProperty(value = "group names")
    private List<String> groupNames;

    @ApiModelProperty(value = "raw log")
    private String rawLog;

    @ApiModelProperty(value = "log path")
    private String logPath;

    @ApiModelProperty(value = "group data")
    private Map<String, String> groupData;

    @ApiModelProperty(value = "log timestamp")
    private Integer logTimestamp;

    public Map<String, Object> genDoc() throws Exception {
        Map<String, Object> res = new HashMap<>();
        Field[] fileds = this.getClass().getDeclaredFields();
        for (Field field : fileds) {
            String key = field.getName();
            String method = key.substring(0, 1).toUpperCase() + key.substring(1);
            Method getMethod = this.getClass().getMethod("get" + method);
            switch (field.getName()) {
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
        res.put("docId", UUID.randomUUID().toString());
        return res;
    }
}
