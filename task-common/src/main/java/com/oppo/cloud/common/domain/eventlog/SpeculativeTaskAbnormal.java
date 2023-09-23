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

package com.oppo.cloud.common.domain.eventlog;

import com.oppo.cloud.common.domain.eventlog.config.SpeculativeTaskConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeculativeTaskAbnormal {

    private Integer jobId;
    private Integer stageId;
    private Integer attemptId;
    /**
     * 推测执行数量
     */
    private Integer speculativeCount;
    /**
     * 推测执行任务id列表
     */
    private List<Long> taskIds;
    private Boolean abnormal;
    private Long threshold;

}
