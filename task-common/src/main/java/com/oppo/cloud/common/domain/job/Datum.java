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

package com.oppo.cloud.common.domain.job;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
public class Datum {

    @ApiModelProperty(value = "节点")
    Set<Node> nodeList;

    @ApiModelProperty(value = "边")
    List<Verge> vergeList;

    @Data
    public static class Node {

        @ApiModelProperty(value = "任务编码")
        private Integer id;
        @ApiModelProperty(value = "任务名称")
        private String taskName;
        @ApiModelProperty(value = "flow名称")
        private String flowName;
        @ApiModelProperty(value = "项目名称")
        private String projectName;
        @ApiModelProperty(value = "任务开始时间")
        private String startTime;
        @ApiModelProperty(value = "任务结束时间")
        private String endTime;
        @ApiModelProperty(value = "任务执行周期")
        private String executionDate;
        @ApiModelProperty(value = "任务运行运行耗时")
        private String duration;
        @ApiModelProperty(value = "运行耗时基线")
        private String durationBaseLine;
        @ApiModelProperty(value = "结束时间基线")
        private String endTimeBaseLine;
        @ApiModelProperty(value = "是否结束时间异常")
        private Boolean endTimeAbnormal = false;
        @ApiModelProperty(value = "是否运行耗时异常")
        private Boolean durationAbnormal = false;
        @ApiModelProperty(value = "任务运行时间段")
        private String period;
        @ApiModelProperty(value = "任务状态")
        private String taskState;

        // 去重
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Node other = (Node) obj;
            return this.id.equals(other.id);
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }
    }

    @Data
    public static class Verge {

        @ApiModelProperty(value = "上游任务")
        private Integer upstream;

        @ApiModelProperty(value = "下游任务")
        private Integer downStream;

        public Verge(Integer upstream, Integer downStream) {
            this.upstream = upstream;
            this.downStream = downStream;
        }
    }
}
