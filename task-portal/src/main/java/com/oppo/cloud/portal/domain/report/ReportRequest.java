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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oppo.cloud.common.util.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 报告总览接口参数
 */
@Data
public class ReportRequest {

    @ApiModelProperty(value = "项目名")
    private String projectName;

    @ApiModelProperty(value = "开始时间")
    private Long start;

    @ApiModelProperty(value = "结束时间")
    private Long end;

    @ApiModelProperty(value = "趋势图类型, 可选值：cpuTrend(cpu消耗趋势), memoryTrend(cpu消耗趋势), numTrend(数量趋势), " +
            "distribution(资源/数量分布)")
    private String graphType;

    private String username;

    @JsonIgnore
    public Map<String, Object> getTermQuery() {
        Map<String, Object> termQuery = new HashMap<>();

        if (StringUtils.isNotEmpty(projectName)) {
            termQuery.put("projectName.keyword", projectName);
        }
        if (StringUtils.isNotBlank(username)) {
            termQuery.put("users.username", username);
        }
        return termQuery;
    }

    @JsonIgnore
    public HashMap<String, Object[]> getRangeConditions() {
        if (start == 0 && end == 0) {
            return null;
        }
        HashMap<String, Object[]> rangeConditions = new HashMap<>();
        Object[] values = new Object[2];
        if (start != 0) {
            values[0] = DateUtil.timestampToUTCDate(start * 1000);
        }
        if (end != 0) {
            values[1] = DateUtil.timestampToUTCDate(end * 1000);
        }
        rangeConditions.put("executionDate", values);
        return rangeConditions;
    }
}
