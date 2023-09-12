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

package com.oppo.cloud.common.domain.flink.report;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 诊断规则线状图表
 */
@Data
public class DiagnosisRuleLineChart implements IDiagnosisRuleChart{
    /**
     * 图表类型
     */
    String type = "Line";
    /**
     * 标题
     */
    String title;
    /**
     * y轴单位
     */
    String yAxisUnit;
    /**
     * y轴值类型
     */
    String yAxisValueType;
    /**
     * 线
     */
    DiagnosisRuleLine line;
    /**
     * 常数线
     */
    Map<String,Double> constLines;
    /**
     * y轴最大值
     */
    Double yAxisMax;
    /**
     * y轴最小值
     */
    Double yAxisMin;
}
