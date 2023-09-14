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

package com.oppo.cloud.portal.domain.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oppo.cloud.common.util.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.search.sort.SortOrder;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class JobDetailRequest {

    @ApiModelProperty("项目名称")
    @NotNull
    private String projectName;

    @ApiModelProperty("流程名称")
    @NotNull
    private String flowName;

    @ApiModelProperty("任务名称")
    @NotNull
    private String taskName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    @ApiModelProperty("执行周期")
    @NotNull
    private Date executionDate;

    @ApiModelProperty("Job第几次重试")
    private Integer tryNumber;

    public JobDetailRequest() {

    }

    public JobDetailRequest(String projectName, String flowName, String taskName, Date executionDate) {
        this.projectName = projectName;
        this.flowName = flowName;
        this.taskName = taskName;
        this.executionDate = executionDate;
    }

    @JsonIgnore
    public Map<String, Object> getTermQuery() {
        Map<String, Object> termQuery = new HashMap<>();

        if (StringUtils.isNotEmpty(projectName)) {
            termQuery.put("projectName.keyword", projectName);
        }
        if (StringUtils.isNotEmpty(flowName)) {
            termQuery.put("flowName.keyword", flowName);
        }
        if (StringUtils.isNotEmpty(taskName)) {
            termQuery.put("taskName.keyword", taskName);
        }
        if (executionDate != null) {
            termQuery.put("executionDate", DateUtil.timestampToUTCDate(executionDate.getTime()));
        }
        return termQuery;
    }

    @JsonIgnore
    public HashMap<String, SortOrder> getSortConditions() {
        HashMap<String, SortOrder> sortConditions = new HashMap<>();
        sortConditions.put("createTime", SortOrder.DESC);
        return sortConditions;
    }
}
