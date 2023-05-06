package com.oppo.cloud.diagnosis.domain.diagnosis;


import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
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
    /**
     * 诊断规则
     */
    private String ruleName;
    /**
     * 作业名字
     */
    private String jobName;
    /**
     * 建议类型
     */
    private FlinkRule adviceType;
    /**
     * 建议描述
     */
    private String adviceDescription;
    /**
     * 是否有建议
     */
    private Boolean hasAdvice = false;
    /**
     * 诊断并行度
     */
    private Integer diagnosisParallel;
    /**
     * tm slot数量
     */
    private Integer diagnosisTmSlotNum;
    /**
     * 诊断tm数量
     */
    private Integer diagnosisTmNum;
    /**
     * 诊断jm内存
     */
    private Integer diagnosisJmMem;
    /**
     * 诊断tm内存
     */
    private Integer diagnosisTmMem;
    /**
     * 诊断tm core
     */
    private Integer diagnosisTmCore;
    /**
     * 慢算子task name
     */
    private String slowTasks;
    /**
     * manage内存MB
     */
    private Integer diagnosisManageMem;
    /**
     * 波谷建议tm个数
     */
    private Integer trafficElasticTmNum;
    // 波谷开始时间
    /**
     *
     */
    private LocalDateTime trafficTroughStartTime;
    // 波谷结束时间
    private LocalDateTime trafficTroughEndTime;
    DiagnosisRuleReport diagnosisRuleReport;
}
