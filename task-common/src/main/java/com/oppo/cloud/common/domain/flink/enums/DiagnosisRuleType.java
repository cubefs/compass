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

/**
 * 诊断规则类型
 */
@Getter
public enum DiagnosisRuleType {
    /**
     * 资源优化类型
     */
    ResourceRule(0, "资源优化类型"),
    /**
     * 运行时异常类型
     */
    RuntimeExceptionRule(1, "运行时异常类型"),
    ;
    private final int code;
    private final String name;

    DiagnosisRuleType(int code, String name) {
        this.code = code;
        this.name = name;
    }
}
