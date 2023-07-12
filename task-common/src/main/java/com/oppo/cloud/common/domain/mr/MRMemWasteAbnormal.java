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

package com.oppo.cloud.common.domain.mr;

import com.oppo.cloud.common.domain.gc.ExecutorPeakMemory;
import com.oppo.cloud.common.domain.gc.GCReport;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MRMemWasteAbnormal {

    private Boolean abnormal;
    /**
     * unit:MB
     */
    private Long mapMemory;
    /**
     * unit:MB
     */
    private Long reduceMemory;

    private Double mapWastePercent;

    private Double reduceWastePercent;
    /**
     * unit:MB
     */
    List<MRTaskMemPeak> mapTaskMemPeakList;
    /**
     * unit:MB
     */
    List<MRTaskMemPeak> reduceTaskMemPeakList;

    public MRMemWasteAbnormal() {
        mapTaskMemPeakList = new ArrayList<>();
        reduceTaskMemPeakList = new ArrayList<>();
    }
}
