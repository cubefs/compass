package com.oppo.cloud.portal.domain.realtime;

import lombok.Data;

import java.util.Map;

@Data
public class DiagnosisGeneralVIewDistributeResp {
    Map<Integer,Long> cpuDistribute;
    Map<Integer,Long> memDistribute;
    Map<Integer,Long> taskNumDistribute;
}
