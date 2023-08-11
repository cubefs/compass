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

/**
 * 诊断报告Y轴类型
 */
public enum DiagnosisReportYAxisType {
    /**
     * 百分比
     */
    Percent(0, "percent 0-1"),
    /**
     * 数值
     */
    Numeric(1, "数值"),
    /**
     * 时间秒
     */
    Second(2, "时间秒"),
    /**
     * 速度bytes/s
     */
    BytesPerSecond(3, "byte/s"),
    ;

    private final int code;
    private final String desc;

    DiagnosisReportYAxisType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
