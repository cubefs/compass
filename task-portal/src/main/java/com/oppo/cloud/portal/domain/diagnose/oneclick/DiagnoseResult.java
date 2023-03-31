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
@ApiModel("一键诊断结果")
public class DiagnoseResult {

    @ApiModelProperty(value = "诊断状态(failed, success, processing)")
    private String status;

    @ApiModelProperty(value = "过程信息")
    private List<ProcessInfo> processInfoList = new ArrayList<>();

    @ApiModelProperty(value = "诊断结果")
    private TaskAppInfo taskAppInfo;

    @ApiModelProperty(value = "异常信息")
    private String errorMsg;

    @Data
    @ApiModel("过程信息")
    public static class ProcessInfo {

        @ApiModelProperty(value = "信息")
        private String msg;
        @ApiModelProperty(value = "进度")
        private double speed;
        public ProcessInfo(String msg, double speed) {
            this.msg = msg;
            this.speed = speed;
        }
    }
}
