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

package com.oppo.cloud.portal.domain.report;

import lombok.Data;

/**
 * 趋势图数据
 */
@Data
public class TrendGraph {

    /**
     * 趋势图名称
     */
    private String name;
    /**
     * 单位
     */
    private String unit;
    /**
     * 异常任务消耗数
     */
    private LineGraph jobUsage;
    /**
     * 总消耗数
     */
    private LineGraph totalUsage;

}
