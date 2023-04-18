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
import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.domain.job.Datum;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.domain.diagnose.runtime.ChartData;
import com.oppo.cloud.portal.domain.diagnose.runtime.TableData;
import com.oppo.cloud.portal.domain.log.LogInfo;
import com.oppo.cloud.portal.domain.task.*;
import com.oppo.cloud.portal.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 作业相关接口
 */
@RestController
@RequestMapping(value = "/api/v1/job")
@Api(value = "JobController", description = "job interface")
@Slf4j
public class JobController {

    @Autowired
    private JobService jobService;

    /**
     * 获取Job列表
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/list")
    @ApiOperation(value = "Job list", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<JobsResponse> searchJobs(@Validated @RequestBody JobsRequest request) throws Exception {
        return CommonStatus.success(jobService.searchJobs(request));
    }

    /**
     * 获取Job下面的app列表
     *
     * @return
     */
    @GetMapping(value = "/apps")
    @ApiOperation(value = "App list", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskName", value = "任务名称", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = "flowName", value = "dagId", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = "projectName", value = "projectName", dataType = "String", dataTypeClass = String.class),
            @ApiImplicitParam(name = "executionDate", value = "执行周期:2021-07-16 15:57:06", dataType = "String", dataTypeClass = String.class)
    })
    public CommonStatus<JobAppsRespone> searchJobs(@RequestParam(value = "taskName") String taskName,
                                                   @RequestParam(value = "flowName") String flowName,
                                                   @RequestParam(value = "projectName") String projectName,
                                                   @RequestParam(value = "executionDate") String executionDate) throws Exception {

        JobDetailRequest jobDetailRequest =
                new JobDetailRequest(projectName, flowName, taskName, DateUtil.parseStrToDate(executionDate));
        return CommonStatus.success(jobService.searchJobApps(jobDetailRequest));

    }

    @PostMapping(value = "/summary")
    @ApiOperation(value = "诊断汇总")
    public CommonStatus<List<String>> getTaskSummary(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        List<String> res = jobService.searchJobDiagnose(jobDetailRequest);
        return CommonStatus.success(res);
    }

    @PostMapping(value = "/jobDiagnoseInfo")
    @ApiOperation(value = "任务级别诊断信息")
    public CommonStatus<List<Item>> getJobDiagnoseInfo(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        List<Item> res = new ArrayList<>();
        res.add(jobService.searchDurationTrend(jobDetailRequest));
        res.add(jobService.searchJobDatum(jobDetailRequest));
        res.add(jobService.searchLogInfo(jobDetailRequest));
        return CommonStatus.success(res);
    }

    @PostMapping(value = "/appDiagnoseInfo")
    @ApiOperation(value = "app级别诊断信息")
    public CommonStatus<List<Map<String, Item>>> getAppDiagnoseInfo(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        return CommonStatus.success(jobService.searchAppDiagnoseInfo(jobDetailRequest));
    }

    @PostMapping(value = "/graph")
    @ApiOperation(value = "job graph")
    public CommonStatus<TrendGraph> getGraph(@Validated @RequestBody JobsRequest request) throws Exception {
        return CommonStatus.success(jobService.getGraph(request));
    }

    @GetMapping(value = "/categories")
    @ApiOperation(value = "app category type")
    public CommonStatus<List<String>> getCategories() {
        List<String> res = new ArrayList<>();
        res.addAll(JobCategoryEnum.getAllAppCategoryOfChina());
        res.addAll(AppCategoryEnum.getAllAppCategoryOfChina());
        return CommonStatus.success(res);
    }

    @PostMapping(value = "/updateState")
    @ApiOperation(value = "更新任务状态", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<String> updateTaskState(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        jobService.updateJobState(jobDetailRequest);
        return CommonStatus.success("ok");
    }

    @Deprecated
    @PostMapping(value = "/log")
    @ApiOperation(value = "异常日志分析")
    public CommonStatus<Item<TableData<LogInfo>>> getExceptionLogs(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        return CommonStatus.success(jobService.searchLogInfo(jobDetailRequest));
    }

    @Deprecated
    @PostMapping(value = "/durationTrend")
    @ApiOperation(value = "运行耗时趋势图")
    public CommonStatus<Item<ChartData>> getHistoryData(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        return CommonStatus.success(jobService.searchDurationTrend(jobDetailRequest));
    }

    @Deprecated
    @PostMapping(value = "/baseline")
    @ApiOperation(value = "基线任务详情")
    public CommonStatus<Item<Datum>> getBaseline(@RequestBody JobDetailRequest jobDetailRequest) throws Exception {
        return CommonStatus.success(jobService.searchJobDatum(jobDetailRequest));
    }
}
