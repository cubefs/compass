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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


@Data
@ApiModel("all job instance info will save in this index of es")
public class JobInstance extends OpenSearchInfo {

    @ApiModelProperty(value = "users")
    private List<SimpleUser> users;

    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "project Id")
    private Integer projectId;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "flow Id")
    private Integer flowId;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "task Id")
    private Integer taskId;

    @ApiModelProperty(value = "execution date")
    private Date executionDate;

    @ApiModelProperty(value = "start time")
    private Date startTime;

    @ApiModelProperty(value = "end time")
    private Date endTime;

    @ApiModelProperty(value = "duration")
    private Double duration;

    @ApiModelProperty(value = "task state")
    private String taskState;

    @ApiModelProperty(value = "memory·seconds")
    private Double memorySeconds;

    @ApiModelProperty(value = "vcore·seconds")
    private Double vcoreSeconds;

    @ApiModelProperty(value = "task type")
    private String taskType;

    @ApiModelProperty(value = "retry times")
    private Integer retryTimes;

    @ApiModelProperty(value = "create time")
    private Date createTime;

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
                case "startTime":
                case "endTime":
                case "updateTime":
                case "createTime":
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

    public String genIndex(String baseIndex) {
        return StringUtils.isNotBlank(this.getIndex()) ? this.getIndex()
                : baseIndex + "-" + DateUtil.format(this.getExecutionDate(), "yyyy-MM-dd");
    }

    public String genDocId() {
        return StringUtils.isNotBlank(this.getDocId()) ? this.getDocId() : UUID.randomUUID().toString();
    }

}
