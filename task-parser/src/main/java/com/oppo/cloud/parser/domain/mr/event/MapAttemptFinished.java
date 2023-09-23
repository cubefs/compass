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

package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.List;

@Data
public class MapAttemptFinished {
    private String taskid;
    private String attemptId;
    private String taskType;
    private String taskStatus;
    private Long mapFinishTime;
    private Long finishTime;
    private String hostname;
    private Integer port;
    private String rackname;
    private String state;
    private JhCounters counters;
    private List<Integer> clockSplits;
    private List<Integer> cpuUsages;
    private List<Integer> vMemKbytes;
    private List<Integer> physMemKbytes;
}
