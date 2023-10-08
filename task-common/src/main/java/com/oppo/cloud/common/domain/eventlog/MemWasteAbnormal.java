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

import com.oppo.cloud.common.domain.gc.ExecutorPeakMemory;
import com.oppo.cloud.common.domain.gc.GCReport;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MemWasteAbnormal {

    private Boolean abnormal;
    /**
     * App total time
     */
    private Long totalTime;
    /**
     * driver memory
     */
    private Long driverMemory;
    /**
     * executor memory
     */
    private Long executorMemory;
    /**
     * total memory time
     */
    private Long totalMemoryTime;
    /**
     * total consuming memory time
     */
    private Long totalMemoryComputeTime;
    /**
     * waste percent
     */
    private Float wastePercent;
    /**
     * threshold
     */
    private Float threshold;

    List<GCReport> gcReportList;

    List<ExecutorPeakMemory> executorPeakMemoryList;

    public MemWasteAbnormal() {
        gcReportList = new ArrayList<>();
        executorPeakMemoryList = new ArrayList<>();
    }
}
