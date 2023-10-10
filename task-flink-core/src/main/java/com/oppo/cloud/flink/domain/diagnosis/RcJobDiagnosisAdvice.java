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

package com.oppo.cloud.flink.domain.diagnosis;


import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * Diagnosis advice/suggestion.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RcJobDiagnosisAdvice implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * diagnostic rule name
     */
    private String ruleName;
    /**
     * flink job name
     */
    private String jobName;
    /**
     * advice types
     */
    private FlinkRule adviceType;
    /**
     * advice description
     */
    private String adviceDescription;
    /**
     * any advice, yes or no
     */
    private Boolean hasAdvice = false;
    /**
     * parallelism after diagnosis.
     */
    private Integer diagnosisParallel;
    /**
     * number of slots of a tm after diagnosis.
     */
    private Integer diagnosisTmSlotNum;
    /**
     * number of task manger after diagnosis.
     */
    private Integer diagnosisTmNum;
    /**
     * job manager memory after diagnosis.
     */
    private Integer diagnosisJmMem;
    /**
     * task manager memory after diagnosis.
     */
    private Integer diagnosisTmMem;
    /**
     * task manager core (vcore) after diagnosis.
     */
    private Integer diagnosisTmCore;
    /**
     * Slow operator(vertice) task name
     */
    private String slowTasks;
    /**
     * manage memory (in MB)
     */
    private Integer diagnosisManageMem;
    /**
     * Recommended number of TMs for valley wave.
     */
    private Integer trafficElasticTmNum;
    /**
     * Start time of valley wave.
     */
    private LocalDateTime trafficTroughStartTime;
    /**
     * End time of valley wave.
     */
    private LocalDateTime trafficTroughEndTime;

    DiagnosisRuleReport diagnosisRuleReport;
}
