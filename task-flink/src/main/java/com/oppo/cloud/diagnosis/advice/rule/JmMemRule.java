package com.oppo.cloud.diagnosis.advice.rule;

import com.google.common.collect.Lists;
import com.oppo.cloud.common.domain.flink.report.*;
import com.oppo.cloud.diagnosis.advice.BaseRule;
import com.oppo.cloud.diagnosis.constant.DiagnosisParamsConstants;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.diagnosis.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.diagnosis.domain.diagnosis.RcJobDiagnosisAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 根据并行度调整jm内存
 */
@Component
public class JmMemRule extends BaseRule {
    @Autowired
    DiagnosisParamsConstants cons;

    @Override
    public RcJobDiagnosisAdvice advice(DiagnosisContext context) {
        RcJobDiagnosis rcJobDiagnosis = context.getRcJobDiagnosis();
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = getBuilder(context);
        builder.adviceType(FlinkRule.JmMemoryRule);
        int tmNum = rcJobDiagnosis.getDiagnosisTmNum();
        Integer adviceJmMem;
        StringBuilder descriptionBuilder = new StringBuilder();
        if (tmNum <= cons.jm1gTmNum) {
            adviceJmMem = 1024;
            descriptionBuilder.append("tm数量在").append(cons.jm1gTmNum).append("以内,调整jm内存到1024MB");
        } else {
            adviceJmMem = 2048;
            descriptionBuilder.append("tm数量在").append(cons.jm1gTmNum).append("以上").append("调整jm内存到2048MB");
        }
        if (!Objects.equals(rcJobDiagnosis.getJmMem(), adviceJmMem)) {
            RcJobDiagnosisAdvice build = builder
                    .diagnosisJmMem(adviceJmMem)
                    .hasAdvice(true)
                    .adviceDescription(descriptionBuilder.toString())
                    .build();
            convertAdviceToRcJobDiagnosis(build,context);
            String conclusion = descriptionBuilder.toString();
            DiagnosisRuleReport diagnosisRuleReport = new DiagnosisRuleReport();
            diagnosisRuleReport.setTitle("JM内存分析");
            diagnosisRuleReport.setConclusion(conclusion);
            DiagnosisRuleBarChart diagnosisRuleBarChart = new DiagnosisRuleBarChart();
            diagnosisRuleBarChart.setTitle("TM数量");
            diagnosisRuleBarChart.setYAxisUnit("(个)");
            DiagnosisRulePoint point = new DiagnosisRulePoint();
            point.setKey("TM数量");
            point.setValue((double)tmNum);
            diagnosisRuleBarChart.setBars(Lists.newArrayList(point));
            diagnosisRuleReport.setIDiagnosisRuleCharts(Lists.newArrayList(diagnosisRuleBarChart));
            build.setDiagnosisRuleReport(diagnosisRuleReport);
            return build;
        } else {
            return builder
                    .diagnosisJmMem(adviceJmMem)
                    .hasAdvice(false)
                    .adviceDescription(descriptionBuilder.toString())
                    .build();
        }

    }

}
