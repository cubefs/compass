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

package com.oppo.cloud.common.domain.flink.metric;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Metrics data parsing structure.
 */
@Data
public class MetricResult {
    private String status;
    private Data data;


    @lombok.Data
    public static class Data {
        private String resultType;
        private List<DataResult> result;
    }

    @lombok.Data
    public static class DataResult {
        private Map<String, Object> metric;
        private List values;
    }

    @lombok.Data
    public static class KeyValue {
        // unit second
        private Integer ts;
        private Double value;

        public KeyValue(int ts, Double v) {
            this.ts = ts;
            this.value = v;
        }
    }
}
