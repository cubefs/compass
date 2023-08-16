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

package com.oppo.cloud.flink.advice;


import com.google.common.collect.Lists;
import com.oppo.cloud.flink.advice.rule.*;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 诊断入口
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Component
public class DiagnosisDoctor {
    @Autowired
    AvgCpuLowRule avgCpuLowRule;
    @Autowired
    TmNoTraffic tmNoTraffic;
    @Autowired
    PeakDurationResourceRule peakDurationResourceRule;
    @Autowired
    PeekRatioResourceRule peekRatioResourceRule;
    @Autowired
    AvgCpuHighRule avgCpuHighRule;
    @Autowired
    JmMemRule jmMemRule;
    @Autowired
    JobNoTraffic jobNoTraffic;
    @Autowired
    SlowVerticesRule slowVerticesRule;
    @Autowired
    TmManagedMemory tmManagedMemory;
    @Autowired
    DelayAndCpuNotFullUtilization delayAndCpuNotFullUtilization;
    @Autowired
    BackPressureRule backPressureRule;
    @Autowired
    JobDelayHigh jobDelayHigh;
    @Autowired
    MemHighRule memHighRule;
    @Autowired
    MemLowRule memLowRule;


    public RcJobDiagnosis diagnosis(DiagnosisContext context) {
        context.setDoctor(this);
        Long start = context.getStart();
        Long end = context.getEnd();
        // 从metric 获取实际运行 parallel 和 slot 和 tm number
        DiagnosisContext c = context.getMetricsClient().getMetrics(context, start, end);
        if (c == null) {
            return null;
        }
        if (c.getStopResourceDiagnosis()) {
            return null;
        }
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();

        setBaseTurningParam(rcJobDiagnosis);
        if (context.getDecrRules() == null) {
            context.setDecrRules(Lists.newArrayList(
                    avgCpuLowRule
                    , tmNoTraffic
                    , memLowRule
            ));
        }
        if (context.getIncrRules() == null) {
            context.setIncrRules(Lists.newArrayList(
                    peakDurationResourceRule,
                    peekRatioResourceRule,
                    avgCpuHighRule,
                    delayAndCpuNotFullUtilization,
                    memHighRule
            ));
        }
        if (context.getAttentionRules() == null) {
            context.setAttentionRules(Lists.newArrayList(
                    tmManagedMemory
                    , slowVerticesRule
                    , jmMemRule
                    , jobNoTraffic
                    , backPressureRule
                    , jobDelayHigh
            ));
        }
        List<RcJobDiagnosisAdvice> advices = new ArrayList<>();
        // 每个基本规则都应用一遍
        for (IAdviceRule rule : context.getAttentionRules()) {
            try {
                RcJobDiagnosisAdvice advice = rule.advice(context);
                if (advice != null) {
                    advices.add(advice);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // 扩容规则应用,最多应用一个
        if (!c.getStopResourceDiagnosis()) {
            boolean isIncreaseResource = false;
            for (IAdviceRule rule : context.getIncrRules()) {
                try {
                    RcJobDiagnosisAdvice advice = rule.advice(context);
                    if (advice != null) {
                        advices.add(advice);
                    }
                    if (advice != null && advice.getHasAdvice()) {
                        isIncreaseResource = true;
                        break;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            // 缩容规则应用,最多应用一个
            if (!isIncreaseResource) {
                for (IAdviceRule rule : context.getDecrRules()) {
                    try {
                        RcJobDiagnosisAdvice advice = rule.advice(context);
                        if (advice != null) {
                            advices.add(advice);
                        }
                        if (advice != null && advice.getHasAdvice()) {
                            break;
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        context.setAdvices(advices);
        return context.getRcJobDiagnosis();
    }


    private List<String> buildTurningChange(RcJobDiagnosis rcJobDiagnosis) {
        List<String> res = new ArrayList<>();
        if (!Objects.equals(rcJobDiagnosis.getTmNum(), rcJobDiagnosis.getDiagnosisTmNum())) {
            res.add(String.format("tm数量:%d->%d", rcJobDiagnosis.getTmNum(), rcJobDiagnosis.getDiagnosisTmNum()));
        } else {
            res.add(String.format("tm数量:%d", rcJobDiagnosis.getTmNum()));
        }
        if (!Objects.equals(rcJobDiagnosis.getDiagnosisParallel(), rcJobDiagnosis.getParallel())) {
            res.add(String.format("任务并行度:%d->%d", rcJobDiagnosis.getParallel(), rcJobDiagnosis.getDiagnosisParallel()));
        } else {
            res.add(String.format("任务并行度:%d", rcJobDiagnosis.getParallel()));
        }
        return res;
    }

    private void convertAdvice(RcJobDiagnosisAdvice advice, RcJobDiagnosis rcJobDiagnosis) {
        if (advice.getDiagnosisParallel() != null) {
            rcJobDiagnosis.setDiagnosisParallel(advice.getDiagnosisParallel());
        }
        if (advice.getDiagnosisTmSlotNum() != null) {
            rcJobDiagnosis.setDiagnosisTmSlot(advice.getDiagnosisTmSlotNum());
        }
        if (advice.getDiagnosisTmNum() != null) {
            rcJobDiagnosis.setDiagnosisTmNum(advice.getDiagnosisTmNum());
        }
        if (advice.getDiagnosisJmMem() != null) {
            rcJobDiagnosis.setDiagnosisJmMem(advice.getDiagnosisJmMem());
        }
        if (advice.getDiagnosisTmMem() != null) {
            rcJobDiagnosis.setDiagnosisTmMem(advice.getDiagnosisTmMem());
        }
        if (advice.getDiagnosisTmCore() != null) {
            rcJobDiagnosis.setDiagnosisTmCore(advice.getDiagnosisTmCore());
        }
    }

    /**
     * 设置诊断资源参数
     *
     * @param rcJobDiagnosis
     */
    private void setBaseTurningParam(RcJobDiagnosis rcJobDiagnosis) {
        // tm core 不配置默认为1
        if (rcJobDiagnosis.getTmCore() == null) {
            rcJobDiagnosis.setTmCore(1);
        }
        rcJobDiagnosis.setDiagnosisTmNum(rcJobDiagnosis.getTmNum());
        rcJobDiagnosis.setDiagnosisParallel(rcJobDiagnosis.getParallel());
        rcJobDiagnosis.setDiagnosisJmMem(rcJobDiagnosis.getJmMem());
        rcJobDiagnosis.setDiagnosisTmMem(rcJobDiagnosis.getTmMem());
        rcJobDiagnosis.setDiagnosisTmCore(rcJobDiagnosis.getTmCore());
        rcJobDiagnosis.setDiagnosisTmSlot(rcJobDiagnosis.getTmSlotNum());
    }

}
