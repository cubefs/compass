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
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.portal.domain.diagnose.DiagnoseReport;
import com.oppo.cloud.portal.domain.diagnose.GCReportResp;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.domain.diagnose.oneclick.DiagnoseResult;
import com.oppo.cloud.portal.domain.diagnose.runerror.RunError;
import com.oppo.cloud.portal.domain.task.*;
import com.oppo.cloud.portal.service.TaskAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * application interface
 */
@RestController
@RequestMapping("/api/v1/app")
@Api(value = "AppController", description = "app interface")
@Slf4j
public class AppController {

    @Autowired
    private TaskAppService taskAppService;

    /**
     * get application list
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/list")
    @ApiOperation(value = "application list", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<TaskAppsResponse> searchApplications(@Validated @RequestBody TaskAppsRequest request) {
        try {
            return CommonStatus.success(taskAppService.searchTaskApps(request));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/report")
    @ApiOperation(value = "diagnose report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<DiagnoseReport> getDiagnoseReport(@RequestParam(value = "applicationId") String applicationId) {
        try {
            return CommonStatus.success(taskAppService.generateReport(applicationId));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/report/runError")
    @ApiOperation(value = "diagnose runError of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<List<Item<RunError>>> getDiagnoseReportRunError(@RequestParam(value = "applicationId") String applicationId) {
        try {
            return CommonStatus.success(taskAppService.diagnoseRunError(applicationId));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/report/runInfo")
    @ApiOperation(value = "diagnose runInfo of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<DiagnoseReport.RunInfo> getDiagnoseReportRunInfo(@RequestParam(value = "applicationId") String applicationId) {
        try {
            return CommonStatus.success(taskAppService.diagnoseRunInfo(applicationId));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/report/runResource")
    @ApiOperation(value = "diagnose runResource of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<List<Item>> getDiagnoseReportRunResource(@RequestParam(value = "applicationId") String applicationId) {
        try {
            return CommonStatus.success(taskAppService.diagnoseRunResource(applicationId));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/report/runTime")
    @ApiOperation(value = "diagnose runTime of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<List<Item>> getDiagnoseReportRunTime(@RequestParam(value = "applicationId") String applicationId) {
        try {
            return CommonStatus.success(taskAppService.diagnoseRunTime(applicationId));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/categories")
    @ApiOperation(value = "app category type")
    public CommonStatus<List<String>> getCategories() {
        try {
            return CommonStatus.success(AppCategoryEnum.getAllAppCategoryOfChina());
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/graph")
    @ApiOperation(value = "task graph")
    public CommonStatus<TrendGraph> getGraph(@Validated @RequestBody JobsRequest request) {
        try {
            return CommonStatus.success(taskAppService.getGraph(request));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/diagnose")
    @ApiOperation(value = "一键诊断")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId名称", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<DiagnoseResult> getAppDiagnose(@RequestParam(value = "applicationId") String applicationId) {
        try {
            if (StringUtils.isNotEmpty(applicationId)) {
                return CommonStatus.success(taskAppService.diagnose(applicationId));
            } else {
                return CommonStatus.failed(String.format("请输入正确的applicationId[%s]信息", applicationId));
            }
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping(value = "/gc")
    @ApiOperation("GC日志分析")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId名称", required = true, dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = "executorId", value = "executor名称", required = true, dataType = "String", dataTypeClass = String.class)})
    public CommonStatus<GCReportResp> getGCReport(@RequestParam(value = "applicationId") String applicationId,
                                                  @RequestParam(value = "executorId") String executorId) {
        try {
            return CommonStatus.success(taskAppService.getGcReport(applicationId, executorId));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }
}
