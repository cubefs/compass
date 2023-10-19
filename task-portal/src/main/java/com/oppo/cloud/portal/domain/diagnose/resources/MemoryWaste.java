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
@ApiModel("MemoryWaste Information")
public class MemoryWaste extends IsAbnormal {

    @ApiModelProperty(value = "chat list")
    private List<Chart<MetricInfo>> chartList = new ArrayList<>();

    @ApiModelProperty(value = "GC information")
    private List<ComputeNode> computeNodeList;

    @Data
    public static class ComputeNode {

        @ApiModelProperty(value = "executor Id")
        private Integer executorId;

        @ApiModelProperty(value = "host name")
        private String hostName;

        @ApiModelProperty(value = "node type(driver/executor)")
        private String nodeType;

    }
}
