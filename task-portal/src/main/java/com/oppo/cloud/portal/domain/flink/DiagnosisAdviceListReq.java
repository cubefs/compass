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
import com.oppo.cloud.portal.domain.task.UserInfo;
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
@ApiModel("实时任务诊断结果分页查询")
public class DiagnosisAdviceListReq {
    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务名称")
    private String jobName;

    @ApiModelProperty(value = "任务状态")
    private String taskState;

    @ApiModelProperty(value = "诊断开始时间秒")
    private Long startTs;

    @ApiModelProperty(value = "诊断结束时间秒")
    private Long endTs;

    @ApiModelProperty(value = "创建者")
    private String username;

    @ApiModelProperty(value = "app id")
    private String applicationId;

    @ApiModelProperty("排序列")
    private String orderColumn;

    @ApiModelProperty("排序顺序")
    private String orderType;

    @ApiModelProperty("包括规则的中文名称")
    private List<String> includeCategories;

    @ApiModelProperty("包括规则")
    private List<Integer> diagnosisRule;

    @ApiModelProperty("排除规则")
    private List<Integer> diagnosisRuleNe;

    @ApiModelProperty("包括资源类型")
    private List<Integer> resourceDiagnosisType;

    @ApiModelProperty("排除资源类型")
    private List<Integer> resourceDiagnosisTypeNe;

    @ApiModelProperty("诊断来源")
    private List<Integer> diagnosisFrom;

    @ApiModelProperty(value = "页码")
    @Min(value = 1, message = "page 不能小于1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量")
    @Max(value = 500, message = "pageSize 不能大于500")
    private Integer pageSize = 15;

    /**
     * 构建ES查询条件
     *
     * @return
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

        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();
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
     * 获取排序字段
     *
     * @return
     */
    @JsonIgnore
    public Map<String, SortOrder> getSortOrder() {
        Map<String, SortOrder> sort = new HashMap<>();
        sort.put("createTime", SortOrder.DESC);
        return sort;
    }

    /**
     * 查询范围条件
     *
     * @return
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
     * 分页开始的条数
     *
     * @return
     */
    @JsonIgnore
    public Integer getFrom() {
        return (page - 1) * pageSize;
    }

    /**
     * 每页大小
     *
     * @return
     */
    @JsonIgnore
    public Integer getSize() {
        return pageSize;
    }
}
