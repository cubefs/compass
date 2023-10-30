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

package com.oppo.cloud.portal.domain.flink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.domain.task.UserInfoResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.search.sort.SortOrder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("Realtime task diagnostic result pagination query")
public class DiagnosisAdviceListReq {
    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "job name")
    private String jobName;

    @ApiModelProperty(value = "task state")
    private String taskState;

    @ApiModelProperty(value = "start timestamp")
    private Long startTs;

    @ApiModelProperty(value = "end timestamp")
    private Long endTs;

    @ApiModelProperty(value = "username")
    private String username;

    @ApiModelProperty(value = "app id")
    private String applicationId;

    @ApiModelProperty("order column")
    private String orderColumn;

    @ApiModelProperty("order type")
    private String orderType;

    @ApiModelProperty("including the Chinese name of the rule")
    private List<String> includeCategories;

    @ApiModelProperty("including the rule")
    private List<Integer> diagnosisRule;

    @ApiModelProperty("exclude the rule")
    private List<Integer> diagnosisRuleNe;

    @ApiModelProperty("including the resource type")
    private List<Integer> resourceDiagnosisType;

    @ApiModelProperty("exclude the resource type")
    private List<Integer> resourceDiagnosisTypeNe;

    @ApiModelProperty("diagnostic source")
    private List<Integer> diagnosisFrom;

    @ApiModelProperty(value = "page")
    @Min(value = 1, message = "page cannot be less than 1")
    private Integer page = 1;

    @ApiModelProperty(value = "number per page")
    @Max(value = 500, message = "pageSize cannot be greater than 500")
    private Integer pageSize = 15;

    /**
     * Get TermQuery
     */
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

        UserInfoResponse userInfo = ThreadLocalUserInfo.getCurrentUser();
        if (StringUtils.isNotEmpty(username)) {
            termQuery.put("users.username", username);
        } else if (!userInfo.isAdmin()) {
            termQuery.put("users.username", userInfo.getUsername());
        }

        if (StringUtils.isNotEmpty(applicationId)) {
            termQuery.put("applicationId.keyword", applicationId);
        }
        return termQuery;
    }

    /**
     * Get sort order
     */
    @JsonIgnore
    public Map<String, SortOrder> getSortOrder() {
        Map<String, SortOrder> sort = new HashMap<>();
        sort.put("createTime", SortOrder.DESC);
        return sort;
    }

    /**
     * Get range condition
     */
    @JsonIgnore
    public Map<String, Object[]> getRangeCondition() {
        if (startTs == null || startTs == 0 || endTs == null || endTs == 0) { // invalid parameters
            this.endTs = System.currentTimeMillis();
            this.startTs = this.endTs - 30 * 24 * 3600 * 1000L;
        }
        if (startTs.toString().length() < 13) {
            this.startTs *= 1000; // to millis
            this.endTs *= 1000; // to millis
        }

        Map<String, Object[]> rangeConditions = new HashMap<>();
        Object[] values = new Object[2];
        values[0] = DateUtil.timestampToUTCDate(startTs);
        values[1] = DateUtil.timestampToUTCDate(endTs);

        rangeConditions.put("createTime", values);
        return rangeConditions;
    }

    /**
     * Get from page number
     */
    @JsonIgnore
    public Integer getFrom() {
        return (page - 1) * pageSize;
    }

    /**
     * Get page size
     */
    @JsonIgnore
    public Integer getSize() {
        return pageSize;
    }
}
