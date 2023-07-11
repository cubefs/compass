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

package com.oppo.cloud.flink.advice.rule;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.metric.MetricResult;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleBarChart;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRulePoint;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.flink.advice.BaseRule;
import com.oppo.cloud.flink.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;
import com.oppo.cloud.flink.util.MonitorMetricUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.oppo.cloud.flink.constant.MonitorMetricConstant.*;


/**
 * 某些tm没有流量，cpu利用率低，堆内存利用率低，则可以缩减掉并行度
 */
@Component
@Slf4j
public class TmNoTraffic extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;
    @Autowired
    MonitorMetricUtil monitorMetricUtil;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.TmNoTraffic);
        List<MetricResult.DataResult> tmFlowList = context.getMetrics().get(TM_DATA_FLOW_RATE);
        List<MetricResult.DataResult> cpuUsageList = context.getMetrics().get(TM_CPU_USAGE_RATE);
        List<MetricResult.DataResult> memUsageList = context.getMetrics().get(TM_HEAP_MEM_USAGE_RATE);
        // 计算流量为0的tm个数
        long tmNoTrafficCount = tmFlowList.stream().map(monitorMetricUtil::getMaxOrNull).filter(d -> d != null && d == 0d).count();
        long tmCpuNoUsageCount = cpuUsageList.stream().map(monitorMetricUtil::getMaxOrNull)
                .filter(d -> d != null && d < cons.getTmCpuNoUsageThreshold()).count();
        long tmMemNoUsageCount = memUsageList.stream().map(monitorMetricUtil::getAvgOfBelowSerrationsOrNull)
                .filter(d -> d != null && d < cons.getTmHeapMemNoUsageThreshold()).count();
        log.debug("{} 作业cpu没有使用的tm个数:{} 内存没使用tm个数:{} 没有流量tm个数:{}", rcJobDiagnosis.getJobName(),
                tmCpuNoUsageCount, tmMemNoUsageCount, tmNoTrafficCount);
        int cutTmNum = Math.min(Math.min((int) tmCpuNoUsageCount, (int) tmMemNoUsageCount), (int) tmNoTrafficCount);
        if (cutTmNum > 0) {
            int minParallel = 1;
            int oriParallel = rcJobDiagnosis.getParallel();
            int tmSlotNum = rcJobDiagnosis.getTmSlotNum();
            int newParallel = oriParallel - tmSlotNum * cutTmNum;
            if (newParallel < minParallel) {
                newParallel = minParallel;
            }
            if (newParallel < oriParallel) {
                int newTmNum = (int) Math.ceil((double) newParallel / tmSlotNum);
                builder.diagnosisParallel(newParallel);
                builder.diagnosisTmNum(newTmNum);
                builder.hasAdvice(true);
                builder.adviceDescription("部分tm没有处理数据,降低并行度");
                RcJobDiagnosisAdvice build = builder.build();
                convertAdviceToRcJobDiagnosis(build, context);
                String resourceChange = buildResourceChange(context);
                String conclusion = String.format("空跑TM数量%d个,该部分CPU,流量,内存都为空,建议缩减并行度,%s"
                        , cutTmNum, resourceChange);
                DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
                diagnosisRuleReport.setTitle("TM空跑分析");
                diagnosisRuleReport.setConclusion(conclusion);
                DiagnosisRuleBarChart diagnosisRuleBarChart = new DiagnosisRuleBarChart();
                diagnosisRuleBarChart.setTitle("TM个数");
                diagnosisRuleBarChart.setYAxisUnit("(个)");
                DiagnosisRulePoint point1 = new DiagnosisRulePoint();
                point1.setKey("空跑TM个数");
                point1.setValue((double) cutTmNum);
                DiagnosisRulePoint point2 = new DiagnosisRulePoint();
                point2.setKey("总TM个数");
                point2.setValue((double) rcJobDiagnosis.getTmNum());
                diagnosisRuleBarChart.setBars(Lists.newArrayList(point1, point2));
                diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleBarChart));
                build.setDiagnosisRuleReport(diagnosisRuleReport);
                return build;
            }

        }
        return builder.hasAdvice(false).adviceDescription("无建议").build();
    }

}
