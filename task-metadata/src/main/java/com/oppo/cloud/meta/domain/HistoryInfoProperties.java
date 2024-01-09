package com.oppo.cloud.meta.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HistoryInfoProperties {
    @JsonProperty("historyInfo")
    private HistoryInfo historyInfo;
}
