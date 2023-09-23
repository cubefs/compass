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

package com.oppo.cloud.portal.domain.diagnose.info;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Parameters of Spark application")
public class AppInfo {

    @ApiModelProperty(value = "spark.driver.memoryOverhead")
    private String driverOverhead;

    @ApiModelProperty(value = "spark.driver.memory")
    private String driverMemory;

    @ApiModelProperty(value = "spark.executor.memoryOverhead")
    private String executorOverhead;

    @ApiModelProperty(value = "spark.executor.memory")
    private String executorMemory;

    @ApiModelProperty(value = "spark.dynamicAllocation.maxExecutors")
    private String maxExecutors;

    @ApiModelProperty(value = "spark.executor.cores")
    private String executorCores;

    @ApiModelProperty(value = "spark.default.parallelism")
    private String parallelism;

    @ApiModelProperty(value = "spark.sql.shuffle.partitions")
    private String shufflePartitions;
}
