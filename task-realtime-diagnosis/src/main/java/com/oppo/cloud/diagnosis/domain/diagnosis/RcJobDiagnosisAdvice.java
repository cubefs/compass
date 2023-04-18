package com.oppo.cloud.diagnosis.domain.diagnosis;


import com.oppo.cloud.common.domain.flink.enums.EDiagnosisRule;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 诊断建议
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RcJobDiagnosisAdvice implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ruleName;
    private String jobName;
    private EDiagnosisRule adviceType;
    private String adviceDescription;

    private Boolean hasAdvice = false;
    // 并行度
    private Integer diagnosisParallel;
    // tm slot数量
    private Integer diagnosisTmSlotNum;
    // tm 数量
    private Integer diagnosisTmNum;
    // jm 内存
    private Integer diagnosisJmMem;
    // tm mem MB
    private Integer diagnosisTmMem;
    // tm core
    private Integer diagnosisTmCore;
    // 慢算子task name
    private String slowTasks;
    // manage内存MB
    private Integer diagnosisManageMem;
    // 波谷建议tm个数
    private Integer trafficElasticTmNum;
    // 波谷开始时间
    private LocalDateTime trafficTroughStartTime;
    // 波谷结束时间
    private LocalDateTime trafficTroughEndTime;
    DiagnosisRuleReport diagnosisRuleReport;
}
