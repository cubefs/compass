/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.common.domain.flink.enums;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Flink diagnosis rules.
 */
@Getter
@Slf4j
public enum FlinkRule {
    /**
     * High memory utilization rate.
     */
    TmMemoryHigh(0, "内存利用率高", "TmMemoryHigh", "rgb(255, 114, 46)", "计算内存的使用率，如果使用率高于阈值，则增加内存",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * Low memory utilization rate.
     */
    TmMemoryLow(1, "内存利用率低", "TmMemoryLow", "rgb(255, 114, 46)", "计算内存的使用率，如果使用率低于阈值，则降低内存",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * JM memory optimization.
     */
    JmMemoryRule(2, "JM内存优化", "JmMemoryRule", "rgb(255, 114, 46)", "根据tm个数计算jm内存的建议值",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * No traffic for the job.
     */
    JobNoTraffic(3, "作业无流量", "JobNoTraffic", "rgb(255, 114, 46)", "检测作业的kafka source算子是否没有流量",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * Slow operators(vertices) exist.
     */
    SlowVerticesRule(4, "存在慢算子", "SlowVerticesRule", "rgb(255, 114, 46)", "检测作业是否存在慢算子",
            DiagnosisRuleType.RuntimeExceptionRule.getCode()),
    /**
     * TM memory management optimization.
     */
    TmManagedMemory(5, "TM管理内存优化", "TmManagedMemory", "rgb(255, 114, 46)",
            "计算作业管理内存的使用率，给出合适的管理内存建议值", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * Partial TM idling.
     */
    TmNoTraffic(7, "部分TM空跑", "TmNoTraffic", "rgb(255, 114, 46)", "检测是否有tm没有流量，并且cpu和内存也没有使用",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * Insufficient parallelism.
     */
    ParallelIncr(8, "并行度不够", "ParallelIncr", "rgb(255, 114, 46)", "检测作业是否因为并行度不够引起延迟",
            DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * High CPU average utilization rate.
     */
    AvgCpuHighRule(10, "CPU利用率高", "AvgCpuHighRule", "rgb(255, 114, 46)",
            "计算作业的CPU使用率，如果高于阈值，则增加cpu", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * Low CPU average utilization rate.
     */
    AvgCpuLowRule(11, "CPU利用率低", "AvgCpuLowRule", "rgb(255, 114, 46)",
            "计算作业的CPU使用率，如果低于阈值，则降低cpu", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * High CPU peak utilization rate.
     */
        PeekDurationResourceRule(12, "CPU峰值利用率高", "PeekDurationResourceRule", "rgb(255, 114, 46)",
            "计算作业的CPU峰值使用率，如果高于阈值，则增加cpu", DiagnosisRuleType.ResourceRule.getCode()),
    /**
     * Back pressure vertices exist.
     */
    BackPressure(15, "存在反压算子", "BackPressure", "rgb(255, 114, 46)",
            "检测作业是否存在反压算子", DiagnosisRuleType.RuntimeExceptionRule.getCode()),
    /**
     * High job latency.
     */
        JobDelay(16, "作业延迟高", "JobDelay", "rgb(255, 114, 46)", "检测作业的kafka延迟是否高于阈值",
            DiagnosisRuleType.RuntimeExceptionRule.getCode()),
    ;
    @JSONField(value = true)
    private final int code;

    @JSONField(value = true)
    private final String zh;

    @JSONField(value = true)
    private final String en;

    @JSONField(value = true)
    private final String color;

    @JSONField(value = true)
    private final String desc;

    @JSONField(value = true)
    private final int ruleType;

    private static final Map<String, FlinkRule> MAP;

    FlinkRule(int code, String zh, String en, String color, String desc, int ruleType) {
        this.code = code;
        this.zh = zh;
        this.en = en;
        this.color = color;
        this.desc = desc;
        this.ruleType = ruleType;
    }

    static {
        Map<String, FlinkRule> map = new ConcurrentHashMap<>();
        for (FlinkRule instance : FlinkRule.values()) {
            map.put(instance.name(), instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public static String getLangMsgByName(String name) {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return MAP.get(name).getZh();
        } else {
            return MAP.get(name).getEn();
        }
    }

    public static List<String> getLangMsgByNameList(List<String> nameList) {
        List<String> langList = new ArrayList<>();
        if (nameList == null) {
            return langList;
        }
        for (String name : nameList) {
            if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
                langList.add(MAP.get(name).getZh());
            } else {
                langList.add(MAP.get(name).getEn());
            }
        }
        return langList;
    }

    public static Integer getCodeByLangMsg(String msg) {
        for (FlinkRule fr : FlinkRule.values()) {
            String ruleMsg;
            if (Locale.SIMPLIFIED_CHINESE.equals(LocaleContextHolder.getLocale())) {
                ruleMsg = fr.getZh();
            } else {
                ruleMsg = fr.getEn();
            }
            if (ruleMsg.equals(msg)) {
                return fr.getCode();
            }
        }
        return 0;
    }

}
