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

import lombok.Getter;

@Getter
public enum DiagnosisFrom {
    /**
     * 每日定时诊断
     */
    EveryDay(0, "每日定时诊断"),
    /**
     * 作业上线诊断
     */
    JobUptime(1, "作业上线诊断"),
    /**
     * 即时诊断
     */
    Manual(2, "即时诊断"),
    ;

    private final int code;
    private final String desc;

    DiagnosisFrom(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
