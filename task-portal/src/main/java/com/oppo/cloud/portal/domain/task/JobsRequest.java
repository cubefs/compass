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
import org.elasticsearch.search.sort.SortOrder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "作业层列表请求参数")
public class JobsRequest {

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "开始时间")
    private long start = 0;

    @ApiModelProperty(value = "结束时间")
    private long end = 0;

    @ApiModelProperty(value = "创建者")
    private String username;

    @ApiModelProperty(value = "异常类型")
    private List<String> categories;

    @ApiModelProperty(value = "趋势图类型, 可选值：cpuTrend(cpu消耗趋势), memoryTrend(内存消耗趋势), numTrend(数量趋势)")
    private String graphType;

    @ApiModelProperty(value = "页码")
    @Min(value = 1, message = "page 不能小于1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量")
    @Max(value = 500, message = "pageSize 不能大于500")
    private Integer pageSize = 15;

    /**
     * 获取TermQuery, 方便构建elk查询条件
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
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();
        if (StringUtils.isNotBlank(username)) {
            termQuery.put("users.username", username);
        } else if (!userInfo.isAdmin()) {
            termQuery.put("users.username", userInfo.getUsername());
        }
        return termQuery;
    }

    /**
     * 获取排序规则
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
     * 分页开始的条数
     */
    @JsonIgnore
    public Integer getFrom() {
        return (page - 1) * pageSize;
    }

    /**
     * 每页大小
     */
    @JsonIgnore
    public Integer getSize() {
        return pageSize;
    }
}
