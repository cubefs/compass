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

package com.oppo.cloud.portal.domain.diagnose.resources;

import com.oppo.cloud.common.domain.eventlog.CpuWasteAbnormal;
import com.oppo.cloud.common.domain.eventlog.config.CpuWasteConfig;
import com.oppo.cloud.portal.domain.diagnose.Chart;
import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.runtime.base.MetricInfo;
import com.oppo.cloud.portal.util.UnitUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@ApiModel("CpuWaste Information")
public class CpuWasteInfo extends IsAbnormal {

    @ApiModelProperty(value = "waste percent of driver")
    private double driverWastePercent;

    @ApiModelProperty(value = "waste percent of executor")
    private double executorWastePercent;

    @ApiModelProperty(value = "chart information")
    private Map<String, Chart.ChartInfo> dataCategory = new HashMap<>();

    private double driverThreshold;

    private double executorThreshold;

    /**
     * build the information from waste data
     * @param cpuWasteAbnormal
     */

    public void buildInfo(CpuWasteAbnormal cpuWasteAbnormal, CpuWasteConfig cpuWasteConfig) {
        this.driverWastePercent = UnitUtil.transferDouble(cpuWasteAbnormal.getDriverWastedPercentOverAll());
        this.executorWastePercent = UnitUtil.transferDouble(cpuWasteAbnormal.getExecutorWastedPercentOverAll());
        this.driverThreshold = UnitUtil.transferDouble(cpuWasteConfig.getDriverThreshold());
        this.executorThreshold = UnitUtil.transferDouble(cpuWasteConfig.getExecutorThreshold());

        HashMap<String, String> vars = new HashMap<>();
        vars.put("driverWastedPercent", String.format("%.2f%%", cpuWasteAbnormal.getDriverWastedPercentOverAll()));
        vars.put("executorWastedPercent", String.format("%.2f%%", cpuWasteAbnormal.getExecutorWastedPercentOverAll()));
        vars.put("driverThreshold", String.format("%.2f%%", cpuWasteConfig.getDriverThreshold()));
        vars.put("executorThreshold", String.format("%.2f%%", cpuWasteConfig.getExecutorThreshold()));
        vars.put("appConsume", UnitUtil.transferVcoreS(cpuWasteAbnormal.getAppComputeMillisAvailable() / 1000));
        vars.put("jobConsume", UnitUtil.transferVcoreS(cpuWasteAbnormal.getInJobComputeMillisAvailable() / 1000));
        vars.put("taskConsume", UnitUtil.transferVcoreS(cpuWasteAbnormal.getInJobComputeMillisUsed() / 1000));
        vars.put("executorWaste", UnitUtil.transferVcoreS(cpuWasteAbnormal.getInJobComputeMillisAvailable() / 1000
                - cpuWasteAbnormal.getInJobComputeMillisUsed() / 1000));
        vars.put("driverWaste", UnitUtil.transferVcoreS(cpuWasteAbnormal.getAppComputeMillisAvailable() / 1000
                - cpuWasteAbnormal.getInJobComputeMillisAvailable() / 1000));
        this.setVars(vars);
    }

}
