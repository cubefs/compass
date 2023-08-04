package com.oppo.cloud.parser.domain.mr;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpeculationInfo {

    private List<Long> elapsedTime;

    private List<String> taskAttemptIds;

    public SpeculationInfo() {
        this.elapsedTime = new ArrayList<>();
        this.taskAttemptIds = new ArrayList<>();
    }
}
