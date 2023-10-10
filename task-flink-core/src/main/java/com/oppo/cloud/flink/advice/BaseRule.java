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

import com.oppo.cloud.flink.advice.turning.TurningAdvice;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosis;
import com.oppo.cloud.flink.domain.diagnosis.RcJobDiagnosisAdvice;

import java.util.Objects;

public abstract class BaseRule implements IAdviceRule {

    protected RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder getBuilder(DiagnosisContext r) {
        RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder = RcJobDiagnosisAdvice.builder();
        builder
                .ruleName(this.getClass().getName())
                .jobName(r.getRcJobDiagnosis().getJobName())
                .hasAdvice(false)
        ;
        return builder;
    }

    /**
     * Building an advice object
     *
     * @param r
     * @param advice
     * @return
     */
    protected RcJobDiagnosisAdvice buildAdvice(DiagnosisContext r, RcJobDiagnosisAdvice advice) {
        advice.setRuleName(this.getClass().getName());
        advice.setJobName(r.getRcJobDiagnosis().getJobName());
        return advice;
    }

    protected RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder convertTurningToAdviceBuilder(
            TurningAdvice advice, RcJobDiagnosisAdvice.RcJobDiagnosisAdviceBuilder builder) {
        builder.diagnosisTmNum(advice.getTmNum())
                .diagnosisTmCore(advice.getVcore())
                .diagnosisParallel(advice.getParallel())
                .diagnosisTmMem(advice.getTmMem())
                .diagnosisTmSlotNum(advice.getTmSlotNum());
        return builder;
    }

    protected void convertAdviceToRcJobDiagnosis(RcJobDiagnosisAdvice advice, DiagnosisContext r) {
        RcJobDiagnosis rcJobDiagnosis = r.getRcJobDiagnosis();
        if (advice.getDiagnosisTmCore() != null) {
            rcJobDiagnosis.setDiagnosisTmCore(advice.getDiagnosisTmCore());
        }
        if (advice.getDiagnosisParallel() != null) {
            rcJobDiagnosis.setDiagnosisParallel(advice.getDiagnosisParallel());
        }
        if (advice.getDiagnosisTmMem() != null) {
            rcJobDiagnosis.setDiagnosisTmMem(advice.getDiagnosisTmMem());
        }
        if (advice.getDiagnosisJmMem() != null) {
            rcJobDiagnosis.setDiagnosisJmMem(advice.getDiagnosisJmMem());
        }
        if (advice.getDiagnosisTmSlotNum() != null) {
            rcJobDiagnosis.setDiagnosisTmSlot(advice.getDiagnosisTmSlotNum());
        }
        if (advice.getDiagnosisTmNum() != null) {
            rcJobDiagnosis.setDiagnosisTmNum(advice.getDiagnosisTmNum());
        }
    }

    protected String buildResourceChange(DiagnosisContext r) {
        RcJobDiagnosis rcJobDiagnosis = r.getRcJobDiagnosis();
        StringBuilder sb = new StringBuilder();
        if (!Objects.equals(rcJobDiagnosis.getDiagnosisParallel(), rcJobDiagnosis.getParallel())) {
            sb.append("parallel:").append(rcJobDiagnosis.getParallel()).append("->").append(rcJobDiagnosis.getDiagnosisParallel()).append(";");
        }
        if (!Objects.equals(rcJobDiagnosis.getDiagnosisTmSlot(), rcJobDiagnosis.getTmSlotNum())) {
            sb.append("tm slot:").append(rcJobDiagnosis.getTmSlotNum()).append("->").append(rcJobDiagnosis.getDiagnosisTmSlot()).append(";");
        }
        if (!Objects.equals(rcJobDiagnosis.getDiagnosisTmCore(), rcJobDiagnosis.getTmCore())) {
            sb.append("tm core:").append(rcJobDiagnosis.getTmCore()).append("->").append(rcJobDiagnosis.getDiagnosisTmCore()).append(";");
        }
        if (!Objects.equals(rcJobDiagnosis.getDiagnosisTmMem(), rcJobDiagnosis.getTmMem())) {
            sb.append("tm mem:").append(rcJobDiagnosis.getTmMem()).append("->").append(rcJobDiagnosis.getDiagnosisTmMem()).append(";");
        }
        if (!Objects.equals(rcJobDiagnosis.getDiagnosisJmMem(), rcJobDiagnosis.getJmMem())) {
            sb.append("jm mem:").append(rcJobDiagnosis.getJmMem()).append("->").append(rcJobDiagnosis.getDiagnosisJmMem()).append(";");
        }
        return sb.toString();
    }

    protected boolean notNullLt(Float left, Float right) {
        if (left != null && right != null) {
            return left < right;
        } else {
            return false;
        }
    }

    protected boolean notNullLt(Integer left, Integer right) {
        if (left != null && right != null) {
            return left < right;
        } else {
            return false;
        }
    }

    protected boolean notNullLte(Integer left, Integer right) {
        if (left != null && right != null) {
            return left <= right;
        } else {
            return false;
        }
    }

    protected boolean notNullLte(Float left, Float right) {
        if (left != null && right != null) {
            return left <= right;
        } else {
            return false;
        }
    }


    protected boolean notNullGt(Integer left, Integer right) {
        if (left != null && right != null) {
            return left > right;
        } else {
            return false;
        }
    }

    protected boolean notNullGte(Integer left, Integer right) {
        if (left != null && right != null) {
            return left >= right;
        } else {
            return false;
        }
    }


    protected boolean notNullGt(Float left, Float right) {
        if (left != null && right != null) {
            return left > right;
        } else {
            return false;
        }
    }

    protected boolean notNullGte(Float left, Float right) {
        if (left != null && right != null) {
            return left >= right;
        } else {
            return false;
        }
    }


    protected boolean notNullEq(Float left, Float right) {
        if (left != null && right != null) {
            return left.equals(right);
        } else {
            return false;
        }
    }

    protected boolean notNullEq(Integer left, Integer right) {
        if (left != null && right != null) {
            return left.equals(right);
        } else {
            return false;
        }
    }
}
