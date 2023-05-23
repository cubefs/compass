package com.oppo.cloud.portal.domain.realtime;

import com.oppo.cloud.portal.domain.report.DistributionGraph;
import lombok.Data;

import java.util.Map;

@Data
public class DiagnosisGeneralVIewDistributeResp {
    Map<String,Long> cpuDistribute;
    Map<String,Long> memDistribute;
    Map<String,Long> taskNumDistribute;

    /**
     * 分布图：CPU
     */
    private DistributionGraph cpu;

    /**
     * 分布图：内存
     */
    private DistributionGraph mem;

    /**
     * 分布图：数量
     */
    private DistributionGraph num;
}
