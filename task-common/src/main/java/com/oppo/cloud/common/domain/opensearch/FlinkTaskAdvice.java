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

package com.oppo.cloud.common.domain.opensearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Flink diagnostic suggestions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlinkTaskAdvice {
    /**
     * Diagnostic rule name.
     */
    private String ruleName;
    /**
     * Diagnostic rule alias: Low CPU utilization, High peak CPU utilization...
     */
    private String ruleAlias;
    /**
     * Rule code.
     */
    private Integer ruleCode;
    /**
     * Whether the rule has been hit or not.
     */
    private Integer hasAdvice;
    /**
     * Description for the advice.
     */
    private String description;
}
