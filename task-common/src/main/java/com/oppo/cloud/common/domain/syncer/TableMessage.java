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

package com.oppo.cloud.common.domain.syncer;

import lombok.Data;

/**
 * Canal data in Kafka
 */
@Data
public class TableMessage {

    /**
     * Origin data
     */
    private String rawData;
    /**
     * Data
     */
    private String body;
    /**
     * Operation: INSERT/UPDATE/DELETE
     */
    private String eventType;
    /**
     * Table
     */
    private String table;

    public TableMessage(String body, String eventType, String table) {
        this.body = body;
        this.eventType = eventType;
        this.table = table;
    }

    public TableMessage(String rawData, String body, String eventType, String table) {
        this.rawData = rawData;
        this.body = body;
        this.eventType = eventType;
        this.table = table;
    }

    public TableMessage() {
    }
}
