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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
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
@ApiModel(value = "Jobs Request")
public class JobsRequest {

    @ApiModelProperty(value = "project name")
    private String projectName;

    @ApiModelProperty(value = "flow name")
    private String flowName;

    @ApiModelProperty(value = "task name")
    private String taskName;

    @ApiModelProperty(value = "start time")
    private long start = 0;

    @ApiModelProperty(value = "end time")
    private long end = 0;

    @ApiModelProperty(value = "username")
    private String username;

    @ApiModelProperty(value = "categories")
    private List<String> categories;

    @ApiModelProperty(value = "graph type, optional：cpuTrend, memoryTrend, numTrend")
    private String graphType;

    @ApiModelProperty(value = "page")
    @Min(value = 1, message = "page cannot be less than 1")
    private Integer page = 1;

    @ApiModelProperty(value = "Number per page")
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
        if (categories != null && categories.size() != 0) {
            List<String> categoriesEnglish = JobCategoryEnum.getJobCategoryEn(categories);
            categoriesEnglish.addAll(AppCategoryEnum.getAppCategoryEn(categories));
            termQuery.put("categories.keyword", categoriesEnglish);
        }
        UserInfoResponse userInfo = ThreadLocalUserInfo.getCurrentUser();
        if (StringUtils.isNotBlank(username)) {
            termQuery.put("users.username", username);
        } else if (!userInfo.isAdmin()) {
            termQuery.put("users.username", userInfo.getUsername());
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

    @JsonIgnore
    public HashMap<String, Object[]> getRangeConditions() {
        if (start == 0 || end == 0) {
            this.end = System.currentTimeMillis();
            this.start = this.end - 30 * 24 * 3600 * 1000L;
        }
        HashMap<String, Object[]> rangeConditions = new HashMap<>();
        Object[] values = new Object[2];
        if (start != 0) {
            values[0] = DateUtil.timestampToUTCDate(start);
        }
        if (end != 0) {
            values[1] = DateUtil.timestampToUTCDate(end);
        }
        rangeConditions.put("executionDate", values);
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
