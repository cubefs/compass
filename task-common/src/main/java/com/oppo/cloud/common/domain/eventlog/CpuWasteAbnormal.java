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

import com.oppo.cloud.common.domain.eventlog.config.CpuWasteConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CpuWasteAbnormal {

    /**
     * app消耗的计算资源
     */
    private long appComputeMillisAvailable;

    /**
     * job消耗的计算资源
     */
    private long inJobComputeMillisAvailable;

    /**
     * task消耗的计算资源
     */
    private long inJobComputeMillisUsed;

    /**
     * driver浪费的计算资源
     */
    private float driverWastedPercentOverAll;

    /**
     * executor浪费的计算资源
     */
    private float executorWastedPercentOverAll;

    /**
     * executor并发最大数量
     */
    private long maxExecutors;

    /**
     * 核数
     */
    private long executorCores;

}
