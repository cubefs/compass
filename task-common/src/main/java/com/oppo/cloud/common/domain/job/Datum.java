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
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
public class Datum {

    @ApiModelProperty(value = "Node")
    Set<Node> nodeList;

    @ApiModelProperty(value = "Edge")
    List<Verge> vergeList;

    @Data
    public static class Node {

        @ApiModelProperty(value = "Task id")
        private Integer id;
        @ApiModelProperty(value = "Task name")
        private String taskName;
        @ApiModelProperty(value = "Flow name")
        private String flowName;
        @ApiModelProperty(value = "Project name")
        private String projectName;
        @ApiModelProperty(value = "Task start time")
        private String startTime;
        @ApiModelProperty(value = "Task end time")
        private String endTime;
        @ApiModelProperty(value = "Execution Date")
        private String executionDate;
        @ApiModelProperty(value = "Task execution time consumption")
        private String duration;
        @ApiModelProperty(value = "Baseline for execution time consumption")
        private String durationBaseLine;
        @ApiModelProperty(value = "Baseline for completion time")
        private String endTimeBaseLine;
        @ApiModelProperty(value = "Whether the completion time is abnormal")
        private Boolean endTimeAbnormal = false;
        @ApiModelProperty(value = "Whether the execution time is abnormal")
        private Boolean durationAbnormal = false;
        @ApiModelProperty(value = "Time period of task execution")
        private String period;
        @ApiModelProperty(value = "Task state")
        private String taskState;

        // Duplicate
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

        @ApiModelProperty(value = "Upstream task")
        private Integer upstream;

        @ApiModelProperty(value = "Downstream task")
        private Integer downStream;

        public Verge(Integer upstream, Integer downStream) {
            this.upstream = upstream;
            this.downStream = downStream;
        }
    }
}
