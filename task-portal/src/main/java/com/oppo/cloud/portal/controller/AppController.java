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
import com.oppo.cloud.portal.domain.diagnose.GCReportResp;
import com.oppo.cloud.portal.domain.diagnose.oneclick.DiagnoseResult;
import com.oppo.cloud.portal.domain.task.JobsRequest;
import com.oppo.cloud.portal.domain.task.TaskAppsRequest;
import com.oppo.cloud.portal.service.OneClickDiagnosisService;
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

    @Autowired
    private OneClickDiagnosisService oneClickDiagnosisService;

    /**
     * get application list
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/list")
    @ApiOperation(value = "application list", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<?> searchApplications(@Validated @RequestBody TaskAppsRequest request) throws Exception {
        return CommonStatus.success(taskAppService.searchTaskApps(request));
    }

    @GetMapping(value = "/report")
    @ApiOperation(value = "diagnose report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<?> getDiagnoseReport(@RequestParam(value = "applicationId") String applicationId) throws Exception {
        return CommonStatus.success(taskAppService.generateReport(applicationId));
    }

    @GetMapping(value = "/report/runError")
    @ApiOperation(value = "diagnose runError of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<?> getDiagnoseReportRunError(@RequestParam(value = "applicationId") String applicationId) throws Exception {
        return CommonStatus.success(taskAppService.diagnoseRunError(applicationId));
    }

    @GetMapping(value = "/report/runInfo")
    @ApiOperation(value = "diagnose runInfo of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<?> getDiagnoseReportRunInfo(@RequestParam(value = "applicationId") String applicationId) throws Exception {
        return CommonStatus.success(taskAppService.diagnoseRunInfo(applicationId));
    }

    @GetMapping(value = "/report/runResource")
    @ApiOperation(value = "diagnose runResource of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<?> getDiagnoseReportRunResource(@RequestParam(value = "applicationId") String applicationId) throws Exception {
        return CommonStatus.success(taskAppService.diagnoseRunResource(applicationId));
    }

    @GetMapping(value = "/report/runTime")
    @ApiOperation(value = "diagnose runTime of report")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId name", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<?> getDiagnoseReportRunTime(@RequestParam(value = "applicationId") String applicationId) throws Exception {
        return CommonStatus.success(taskAppService.diagnoseRunTime(applicationId));
    }

    @GetMapping(value = "/categories")
    @ApiOperation(value = "app category type")
    public CommonStatus<?> getCategories() {
        return CommonStatus.success(AppCategoryEnum.getAllAppCategoryOfChina());
    }

    @PostMapping(value = "/graph")
    @ApiOperation(value = "task graph")
    public CommonStatus<?> getGraph(@Validated @RequestBody JobsRequest request) throws Exception {
        return CommonStatus.success(taskAppService.getGraph(request));
    }

    @GetMapping(value = "/diagnose")
    @ApiOperation(value = "one-click diagnosis")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId", required = true, dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<DiagnoseResult> getAppDiagnose(@RequestParam(value = "applicationId") String applicationId) throws Exception {
        if (StringUtils.isNotEmpty(applicationId)) {
            return CommonStatus.success(oneClickDiagnosisService.diagnose(applicationId));
        } else {
            return CommonStatus.failed(String.format("Invalid applicationId: %s", applicationId));
        }
    }

    @GetMapping(value = "/gc")
    @ApiOperation("GC log analysis")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "applicationId", value = "applicationId", required = true, dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = "executorId", value = "executor", required = true, dataType = "String", dataTypeClass = String.class)})
    public CommonStatus<GCReportResp> getGCReport(@RequestParam(value = "applicationId") String applicationId,
                                                  @RequestParam(value = "executorId") String executorId) throws Exception {
        return CommonStatus.success(taskAppService.getGcReport(applicationId, executorId));
    }
}
