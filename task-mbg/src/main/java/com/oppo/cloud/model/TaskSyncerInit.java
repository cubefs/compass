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

package com.oppo.cloud.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

public class TaskSyncerInit implements Serializable {

    @ApiModelProperty(value = "task_syncer应用是否已初始化： 0 -> 否, 1 -> 是")
    private Integer isInit;

    private static final long serialVersionUID = 1L;

    public Integer getIsInit() {
        return isInit;
    }

    public void setIsInit(Integer isInit) {
        this.isInit = isInit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", isInit=").append(isInit);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
