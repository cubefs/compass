package com.oppo.cloud.parser.domain.mr;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpeculationInfo {

    private int speculationCount;

    private long speculationElapsedTime;

    private List<String> taskAttemptIds;

    public SpeculationInfo() {
        this.speculationCount = 0;
        this.speculationElapsedTime = 0L;
        this.taskAttemptIds = new ArrayList<>();
    }
}
