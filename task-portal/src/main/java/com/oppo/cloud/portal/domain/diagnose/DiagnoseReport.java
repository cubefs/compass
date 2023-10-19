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

package com.oppo.cloud.portal.domain.diagnose;

import com.oppo.cloud.portal.domain.diagnose.info.AppInfo;
import com.oppo.cloud.portal.domain.diagnose.info.ClusterInfo;
import com.oppo.cloud.portal.domain.diagnose.info.TaskInfo;
import com.oppo.cloud.portal.domain.diagnose.runerror.RunError;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("DiagnoseReport")
public class DiagnoseReport {

    @ApiModelProperty("run information")
    private RunInfo runInfo;

    @ApiModelProperty("run error")
    private List<Item<RunError>> runErrorAnalyze = new ArrayList<>();

    @ApiModelProperty("resources")
    private List<Item> resourcesAnalyze = new ArrayList<>();

    @ApiModelProperty("run time")
    private List<Item> runTimeAnalyze = new ArrayList<>();

    @Data
    public static class RunInfo {

        @ApiModelProperty("task information")
        private TaskInfo taskInfo;

        @ApiModelProperty("cluster information")
        private ClusterInfo clusterInfo;

        @ApiModelProperty("app parameter")
        private Map<String, Object> env;

        @ApiModelProperty("error information")
        private String error;
    }

}
