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

package com.oppo.cloud.portal.domain.diagnose.resources;

import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("内存浪费分析")
public class MemoryWaste extends IsAbnormal {

    @ApiModelProperty(value = "图表信息")
    private List<Chart<MetricInfo>> chartList = new ArrayList<>();

    @ApiModelProperty(value = "GC分析")
    private List<ComputeNode> computeNodeList;

    @Data
    public static class ComputeNode {

        @ApiModelProperty(value = "节点名称")
        private Integer executorId;

        @ApiModelProperty(value = "主机名称")
        private String hostName;

        @ApiModelProperty(value = "节点类型(driver/executor)")
        private String nodeType;

    }
}
