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

package com.oppo.cloud.flink.advice.turning;

import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import lombok.Data;

@Data
public class TurningAdvice {
    private Integer parallel;
    private Integer vcore;
    private Integer tmSlotNum;
    private Integer tmNum;
    private Integer totalCore;
    private Integer totalSlot;
    private Integer tmMem;
    // Full score: 100
    private Float score;
    private DiagnosisTurningStatus status = DiagnosisTurningStatus.NO_ADVICE;
    private String description = "";
}
