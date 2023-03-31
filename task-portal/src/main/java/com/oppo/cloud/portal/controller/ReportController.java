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

package com.oppo.cloud.portal.controller;

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.portal.domain.report.ReportGraph;
import com.oppo.cloud.portal.domain.report.ReportRequest;
import com.oppo.cloud.portal.domain.statistics.StatisticsData;
import com.oppo.cloud.portal.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/report")
@Api(value = "ReportController", description = "报告总览接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(value = "/statistics")
    @ApiOperation(value = "获取任务的指标数据", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectName", value = "项目名称", dataType = "String", dataTypeClass = String.class),
    })
    public CommonStatus<StatisticsData> getStatisticData(@RequestParam(value = "projectName", required = false) String projectName) {
        try {
            return CommonStatus.success(reportService.getStatisticsData(projectName));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/graph")
    @ApiOperation("报告总览图表接口")
    public CommonStatus<ReportGraph> getGraph(@RequestBody ReportRequest reportRequest) {
        try {
            return CommonStatus.success(reportService.getGraph(reportRequest));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @ApiOperation("项目列表")
    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    @ResponseBody
    public CommonStatus<Set<String>> getProjects() {
        try {
            return CommonStatus.success(reportService.getProjects());
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

}
