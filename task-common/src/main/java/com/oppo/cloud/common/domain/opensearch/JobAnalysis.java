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
@ApiModel("all abnormal job instance info will save in this index of es")
public class JobAnalysis extends OpenSearchInfo {

    @ApiModelProperty(value = "用户名称")
    private List<SimpleUser> users;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目Id")
    private Integer projectId;

    @ApiModelProperty(value = "工作流")
    private String flowName;

    @ApiModelProperty(value = "工作流Id")
    private Integer flowId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务Id")
    private Integer taskId;

    @ApiModelProperty(value = "执行周期")
    private Date executionDate;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "任务执行耗时")
    private Double duration;

    @ApiModelProperty(value = "任务执行状态")
    private String taskState;

    @ApiModelProperty(value = "执行所消耗所有memory·seconds")
    private Double memorySeconds;

    @ApiModelProperty(value = "执行所消耗所有vcore·seconds")
    private Double vcoreSeconds;

    @ApiModelProperty(value = "任务类型")
    private String taskType;

    @ApiModelProperty(value = "任务重试次数")
    private Integer retryTimes;

    @ApiModelProperty(value = "诊断类型")
    private List<String> categories;

    @ApiModelProperty(value = "运行耗时基线值")
    private String durationBaseline;

    @ApiModelProperty(value = "结束时间基线值")
    private String endTimeBaseline;

    @ApiModelProperty(value = "最近一次成功时间(长期失败任务)")
    private String successExecutionDay;

    @ApiModelProperty(value = "距离最近一次成功的天数(长期失败任务)")
    private String successDays;

    @ApiModelProperty(value = "任务使用内存(OOM预警)")
    private Double memory;

    @ApiModelProperty(value = "内存占比(OOM预警)")
    private Double memoryRatio;

    @ApiModelProperty(value = "是否删除")
    private Integer deleted = 0;

    @ApiModelProperty(value = "任务处理状态：未处理(0)、已处理(1)")
    private Integer taskStatus = 0;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    public Map<String, Object> genDoc() throws Exception {
        Map<String, Object> doc = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
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
                        doc.put(key, DateUtil.timestampToUTCDate(value.getTime()));
                    }
                    break;
                default:
                    doc.put(key, getMethod.invoke(this));
            }
        }
        return doc;
    }

    public String genIndex(String baseIndex) {
        return StringUtils.isNotBlank(this.getIndex()) ? this.getIndex()
                : baseIndex + "-" + DateUtil.format(this.getExecutionDate(), "yyyy-MM-dd");
    }

    public String genDocId() {
        return StringUtils.isNotBlank(this.getDocId()) ? this.getDocId() : UUID.randomUUID().toString();
    }
}
