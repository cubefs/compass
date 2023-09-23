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

package com.oppo.cloud.portal.domain.diagnose.oneclick;

import com.oppo.cloud.portal.domain.task.TaskAppInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("result of one-click diagnosis")
public class DiagnoseResult {

    @ApiModelProperty(value = "diagnosing status(failed, success, processing)")
    private String status;

    @ApiModelProperty(value = "processing information")
    private List<ProcessInfo> processInfoList = new ArrayList<>();

    @ApiModelProperty(value = "task information")
    private TaskAppInfo taskAppInfo;

    @ApiModelProperty(value = "exception")
    private String errorMsg;

    @Data
    @ApiModel("processing information")
    public static class ProcessInfo {

        @ApiModelProperty(value = "message")
        private String msg;
        @ApiModelProperty(value = "speed")
        private double speed;
        public ProcessInfo(String msg, double speed) {
            this.msg = msg;
            this.speed = speed;
        }
    }
}
