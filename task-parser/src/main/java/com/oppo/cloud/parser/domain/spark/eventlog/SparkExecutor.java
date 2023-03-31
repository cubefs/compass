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

import java.util.ArrayList;
import java.util.List;

@Data
public class SparkExecutor {

    /**
     * executor ID
     */
    private String Id;
    private String host;
    private List<SparkBlockManager> blockManagers;
    private Long startTimestamp;
    private Integer totalCores;
    private List<SparkTask> tasks;
    private String removeReason;
    private Long removeTimestamp;

    public SparkExecutor(SparkListenerExecutorAdded added) {
        this.blockManagers = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.Id = added.getExecutorId();
        this.host = added.getExecutorInfo().getExecutorHost();
        this.startTimestamp = added.getTime();
        this.totalCores = added.getExecutorInfo().getTotalCores();
        this.removeTimestamp = 0L;
    }

    public void addBlockManager(SparkBlockManager blockManager) {
        this.blockManagers.add(blockManager);
    }

    public void remove(SparkListenerExecutorRemoved removed) {
        this.removeReason = removed.getReason();
        if (removed.getTime() != null) {
            this.removeTimestamp = removed.getTime();
        }
    }

}
