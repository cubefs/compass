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

package com.oppo.cloud.flink.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrafficThoughResponse {
    private String jobName;
    // 波谷建议tm个数
    private Integer trafficElasticTmNum;
    // 波谷开始时间
    private LocalDateTime trafficTroughStartTime;
    // 波谷结束时间
    private LocalDateTime trafficTroughEndTime;
}
