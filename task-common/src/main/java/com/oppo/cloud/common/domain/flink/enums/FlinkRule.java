package com.oppo.cloud.common.domain.flink.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

/**
 * Flink 诊断规则
 */
@Getter
@Slf4j
public enum FlinkRule {
    /**
     * 内存利用率高
     */
    TmMemoryHigh(0, "内存利用率高", "rgb(255, 114, 46)", "计算内存的使用率，如果使用率高于阈值，则增加内存",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * 内存利用率低
     */
    TmMemoryLow(1, "内存利用率低", "rgb(255, 114, 46)", "计算内存的使用率，如果使用率低于阈值，则降低内存",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * JM内存优化
     */
    JmMemoryRule(2, "JM内存优化", "rgb(255, 114, 46)", "根据tm个数计算jm内存的建议值",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * 作业无流量
     */
    JobNoTraffic(3, "作业无流量", "rgb(255, 114, 46)", "检测作业的kafka source算子是否没有流量",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * 存在慢算子
     */
    SlowVerticesRule(4, "存在慢算子", "rgb(255, 114, 46)", "检测作业是否存在慢算子",
            DiagnosisRuleType.RuntimeExceptionRule.getCode()),
    /**
     * TM管理内存优化
     */
    TmManagedMemory(5, "TM管理内存优化", "rgb(255, 114, 46)",
            "计算作业管理内存的使用率，给出合适的管理内存建议值", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * 部分TM空跑
     */
    TmNoTraffic(7, "部分TM空跑", "rgb(255, 114, 46)", "检测是否有tm没有流量，并且cpu和内存也没有使用",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * 并行度不够
     */
    ParallelIncr(8, "并行度不够", "rgb(255, 114, 46)", "检测作业是否因为并行度不够引起延迟",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * CPU均值利用率高
     */
    AvgCpuHighRule(10, "CPU利用率高", "rgb(255, 114, 46)",
            "计算作业的CPU使用率，如果高于阈值，则增加cpu", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * CPU均值利用率低
     */
    AvgCpuLowRule(11, "CPU利用率低", "rgb(255, 114, 46)",
            "计算作业的CPU使用率，如果低于阈值，则降低cpu", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * CPU峰值利用率高
     */
    PeekDurationResourceRule(12, "CPU峰值利用率高", "rgb(255, 114, 46)",
            "计算作业的CPU峰值使用率，如果高于阈值，则增加cpu", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * 存在反压算子
     */
    BackPressure(15, "存在反压算子", "rgb(255, 114, 46)",
            "检测作业是否存在反压算子", DiagnosisRuleType.RuntimeExceptionRule.getCode()),
    /**
     * 作业延迟高
     */
    JobDelay(16, "作业延迟高", "rgb(255, 114, 46)", "检测作业的kafka延迟是否高于阈值",
            DiagnosisRuleType.RuntimeExceptionRule.getCode()),
    ;
    @JSONField(value = true)
    private final int code;
    @JSONField(value = true)
    private final String name;
    @JSONField(value = true)
    private final String color;
    @JSONField(value = true)
    private final String desc;
    @JSONField(value = true)
    private final int ruleType;

    FlinkRule(int code, String name, String color, String desc, int ruleType) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.desc = desc;
        this.ruleType = ruleType;
    }

    public static FlinkRule valueOf(int code) {
        return Stream.of(FlinkRule.values()).filter(x -> x.getCode() == code).findFirst().orElse(null);
    }

}
