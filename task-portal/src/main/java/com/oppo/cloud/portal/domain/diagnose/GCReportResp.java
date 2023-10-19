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

package com.oppo.cloud.portal.domain.diagnose;

import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.domain.gc.HeapUsed;
import com.oppo.cloud.common.domain.gc.TenuredUsed;
import com.oppo.cloud.common.domain.gc.YoungUsed;
import com.oppo.cloud.portal.util.UnitUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class GCReportResp {

    @ApiModelProperty(value = "Maximum allocated memory")
    private String maxHeapAllocatedSize;

    @ApiModelProperty(value = "Maximum memory usage")
    private String maxHeapUsedSize;

    @ApiModelProperty(value = "Total time")
    private String totalTime;

    @ApiModelProperty(value = "YG count/time (s)")
    private String YGCountAndDuration;

    @ApiModelProperty(value = "FG count/time (s)")
    private String FGCountAndDuration;

    @ApiModelProperty(value = "GC count/time (s)")
    private String GCCountAndDuration;

    @ApiModelProperty(value = "Heap usage trend chart")
    private List<HeapUsed> heapUsed;

    @ApiModelProperty(value = "Tenured usage trend chart")
    private List<TenuredUsed> tenuredUsed;

    @ApiModelProperty(value = "Young usage trend chart")
    private List<YoungUsed> youngUsed;

    public void build(GCReport gcReport) {
        this.totalTime = gcReport.getTotalTime();
        this.heapUsed = gcReport.getHeapUsed();
        this.tenuredUsed = gcReport.getTenuredUsed();
        this.youngUsed = gcReport.getYoungUsed();
        this.maxHeapAllocatedSize =
                String.format("%.2fGB", UnitUtil.transferKBToGB((long) gcReport.getMaxHeapAllocatedSize()));
        this.maxHeapUsedSize = String.format("%.2fGB", UnitUtil.transferKBToGB((long) gcReport.getMaxHeapUsedSize()));
        this.YGCountAndDuration = String.format("%d/%.2f", gcReport.getYoungGCCount(), gcReport.getYoungGCTime());
        this.FGCountAndDuration = String.format("%d/%.2f", gcReport.getFullGCCount(), gcReport.getFullGCTime());
        this.GCCountAndDuration = String.format("%d/%.2f", gcReport.getTotalGCCount(), gcReport.getTotalGCTime());
    }
}
