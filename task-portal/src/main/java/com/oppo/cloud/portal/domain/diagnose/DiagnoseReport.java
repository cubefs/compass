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

@Data
@ApiModel("诊断报告")
public class DiagnoseReport {

    @ApiModelProperty("运行信息")
    private RunInfo runInfo;

    @ApiModelProperty("运行错误")
    private List<Item<RunError>> runErrorAnalyze = new ArrayList<>();

    @ApiModelProperty("资源使用")
    private List<Item> resourcesAnalyze = new ArrayList<>();

    @ApiModelProperty("运行耗时")
    private List<Item> runTimeAnalyze = new ArrayList<>();

    @Data
    public static class RunInfo {

        @ApiModelProperty("任务运行信息")
        private TaskInfo taskInfo;

        @ApiModelProperty("集群信息")
        private ClusterInfo clusterInfo;

        @ApiModelProperty("app运行参数")
        private AppInfo appInfo;

        @ApiModelProperty("错误信息")
        private String error;
    }

}
