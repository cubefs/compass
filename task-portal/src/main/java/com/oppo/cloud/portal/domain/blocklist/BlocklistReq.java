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

package com.oppo.cloud.portal.domain.blocklist;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("查询白名单列表的请求参数")
public class BlocklistReq {

    @ApiModelProperty(value = "模块")
    private String component;
    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "页码")
    @Min(value = 1, message = "page 不能小于1")
    @NotNull(message = "page 不能为空")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数据量")
    @NotNull(message = "pageSize 不能为空")
    @Max(value = 500, message = "pageSize 不能大于500")
    private Integer pageSize = 10;

}
