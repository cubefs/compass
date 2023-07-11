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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

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
}
