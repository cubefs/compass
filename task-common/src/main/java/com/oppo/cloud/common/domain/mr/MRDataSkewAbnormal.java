package com.oppo.cloud.common.domain.mr;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class MRDataSkewAbnormal {

    private Boolean abnormal;

    private String taskType;

    private Double ratio;

    private Long elapsedTime;

    private List<MRDataSkewGraph> graphList;

    public MRDataSkewAbnormal() {
        graphList = new ArrayList<>();
    }
}
