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
 * 诊断资源类型
 */
@Getter
public enum DiagnosisResourceType {
    /**
     * 扩容CPU
     */
    INCR_CPU(0, "扩容cpu"),
    /**
     * 扩容内存
     */
    INCR_MEM(1, "扩容内存"),
    /**
     * 缩减CPU
     */
    DECR_CPU(2, "缩减cpu"),
    /**
     * 缩减内存
     */
    DECR_MEM(3, "缩减内存"),
    /**
     * 运行一场
     */
    RUNTIME_EXCEPTION(4, "运行异常"),
    ;
    private final int code;
    private final String desc;

    DiagnosisResourceType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
