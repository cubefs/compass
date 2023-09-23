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

package com.oppo.cloud.parser.domain.spark.eventlog;

import lombok.Data;

@Data
public class DetectResult {

    private Long maxResult = 0L;
    private SparkPlanInfo sparkPlanInfo;

    /**
     * @return 返回原始节点名称： Scan orc union_os_dw.dim_model_info
     */
    public String getScanNodeName() {
        if (this.sparkPlanInfo != null) {
            return this.sparkPlanInfo.getNodeName();
        }
        return "scan table";
    }

    /**
     * @return 返回节点表名字 union_os_dw.dim_model_info
     */
    public String getScanNodeTable() {
        if (this.sparkPlanInfo == null) {
            return "scan table";
        }

        String[] fields = this.sparkPlanInfo.getNodeName().split(" ");
        if (fields.length > 0) {
            return fields[fields.length - 1];
        }
        return "scan table";
    }
}
