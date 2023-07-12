package com.oppo.cloud.common.domain.mr;

import lombok.Data;

import java.util.List;


@Data
public class MRDataSkewAbnormal {

    private Double mapRatio;

    private Double reduceRatio;

    private Boolean isMapSkew;

    private Boolean isReduceSkew;

    private List<MRDataSkewGraph> mapGraphList;

    private List<MRDataSkewGraph> reduceGraphList;


}
