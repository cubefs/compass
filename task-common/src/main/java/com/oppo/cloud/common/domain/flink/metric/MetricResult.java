package com.oppo.cloud.common.domain.flink.metric;

import lombok.Data;

import java.util.List;
import java.util.Map;

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
        // 单位 second
        private Integer ts;
        private Double value;

        public KeyValue(int ts, Double v) {
            this.ts = ts;
            this.value = v;
        }
    }
}
