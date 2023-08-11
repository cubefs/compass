package com.oppo.cloud.common.domain.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * flink 诊断建议
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlinkTaskAdvice {
    /**
     * 诊断规则名
     */
    private String ruleName;
    /**
     * 诊断规则别名: CPU利用率低、CPU峰值利用率高...
     */
    private String ruleAlias;
    /**
     * 规则编码
     */
    private Integer ruleCode;
    /**
     * 规则是否命中0未1有
     */
    private Integer hasAdvice;
    /**
     * 描述
     */
    private String description;
}
