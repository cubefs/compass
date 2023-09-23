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

package com.oppo.cloud.portal.domain.flink;

import com.oppo.cloud.portal.domain.report.DistributionGraph;
import lombok.Data;

import java.util.Map;

@Data
public class DiagnosisGeneralVIewDistributeResp {
    Map<String,Long> cpuDistribute;
    Map<String,Long> memDistribute;
    Map<String,Long> taskNumDistribute;

    /**
     * 分布图：CPU
     */
    private DistributionGraph cpu;

    /**
     * 分布图：内存
     */
    private DistributionGraph mem;

    /**
     * 分布图：数量
     */
    private DistributionGraph num;
}
