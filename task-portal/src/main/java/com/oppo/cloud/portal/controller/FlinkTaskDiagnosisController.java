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


import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.api.CommonPage;
import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.common.constant.ComponentEnum;
import com.oppo.cloud.common.domain.opensearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisResourceType;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisRuleType;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.flink.service.DiagnosisService;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.*;
import com.oppo.cloud.portal.domain.flink.*;
import com.oppo.cloud.portal.service.FlinkTaskDiagnosisService;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping(value = "/api/flink")
@Api(value = "FlinkTaskDiagnosisController", description = "Api for flink diagnosis")
@Slf4j
public class FlinkTaskDiagnosisController {

    @Autowired
    FlinkTaskDiagnosisService flinkTaskDiagnosisService;

    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;

    @Autowired
    DiagnosisService diagnosisService;

    @Autowired
    BlocklistMapper blocklistMapper;

    @GetMapping(value = "/diagnosisRules")
    @ApiOperation(value = "get the rules of diagnosis")
    public CommonStatus<?> getDiagnosisRules() {
        FlinkRule[] flinkRules = FlinkRule.values();
        List<DiagnosisRuleResp> list = new ArrayList<>();
        for (FlinkRule ediagnosisRule : flinkRules) {
            DiagnosisRuleResp diagnosisRuleResp = new DiagnosisRuleResp();
            diagnosisRuleResp.setCode(ediagnosisRule.getCode());
            diagnosisRuleResp.setName(FlinkRule.getLangMsgByName(ediagnosisRule.name()));
            diagnosisRuleResp.setDesc(ediagnosisRule.getDesc());
            list.add(diagnosisRuleResp);
        }
        return CommonStatus.success(list);
    }

    @GetMapping(value = "/diagnosisFrom")
    @ApiOperation(value = "get the source of diagnosis")
    public CommonStatus<?> getDiagnosisFromTypes() {
        DiagnosisFrom[] eDiagnosisRules = DiagnosisFrom.values();
        List<DiagnosisFromResp> list = new ArrayList<>();
        for (DiagnosisFrom diagnosisFrom : eDiagnosisRules) {
            DiagnosisFromResp diagnosisFromResp = new DiagnosisFromResp();
            diagnosisFromResp.setCode(diagnosisFrom.getCode());
            diagnosisFromResp.setName(diagnosisFrom.getDesc());
            list.add(diagnosisFromResp);
        }
        // todo:
        return CommonStatus.success(list);
    }


    @GetMapping(value = "/resourceDiagnosisRules")
    @ApiOperation(value = "get the rule information")
    public CommonStatus<?> getResourceDiagnosisRules() {
        FlinkRule[] flinkRules = FlinkRule.values();
        List<DiagnosisRuleResp> list = new ArrayList<>();
        for (FlinkRule ediagnosisRule : flinkRules) {
            if (ediagnosisRule.getRuleType() == DiagnosisRuleType.ResourceRule.getCode()) {
                DiagnosisRuleResp diagnosisRuleResp = new DiagnosisRuleResp();
                diagnosisRuleResp.setCode(ediagnosisRule.getCode());
                diagnosisRuleResp.setName(FlinkRule.getLangMsgByName(ediagnosisRule.name()));
                diagnosisRuleResp.setDesc(ediagnosisRule.getDesc());
                list.add(diagnosisRuleResp);
            }
        }
        return CommonStatus.success(list);
    }

    @GetMapping(value = "/runtimeExceptionDiagnosisTypes")
    @ApiOperation(value = "get runtime exception")
    public CommonStatus<List<DiagnosisRuleResp>> getRuntimeExceptionDiagnosisRules() {
        FlinkRule[] flinkRules = FlinkRule.values();
        List<DiagnosisRuleResp> list = new ArrayList<>();
        for (FlinkRule ediagnosisRule : flinkRules) {
            if (ediagnosisRule.getRuleType() == DiagnosisRuleType.RuntimeExceptionRule.getCode()) {
                DiagnosisRuleResp diagnosisRuleResp = new DiagnosisRuleResp();
                diagnosisRuleResp.setCode(ediagnosisRule.getCode());
                diagnosisRuleResp.setName(FlinkRule.getLangMsgByName(ediagnosisRule.name()));
                diagnosisRuleResp.setDesc(ediagnosisRule.getDesc());
                list.add(diagnosisRuleResp);
            }
        }
        return CommonStatus.success(list);
    }

    @GetMapping(value = "/resourceDiagnosisTypes")
    @ApiOperation(value = "get rule type of diagnosis resource")
    public CommonStatus<List<DiagnosisResourceTypeResp>> getResourceRuleTypes() {
        DiagnosisResourceType[] diagnosisResourceTypes = DiagnosisResourceType.values();
        List<DiagnosisResourceTypeResp> list = new ArrayList<>();
        for (DiagnosisResourceType diagnosisResourceType : diagnosisResourceTypes) {
            DiagnosisResourceTypeResp diagnosisRuleResp = new DiagnosisResourceTypeResp();
            diagnosisRuleResp.setCode(diagnosisResourceType.getCode());
            diagnosisRuleResp.setName(diagnosisResourceType.getDesc());
            list.add(diagnosisRuleResp);
        }
        return CommonStatus.success(list);
    }

    /**
     * Get a list of jobs
     *
     * @return
     */
    @PostMapping(value = "/page")
    @ApiOperation(value = "list of diagnosis jobs", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<?> pageJobs(@Validated @RequestBody DiagnosisAdviceListReq request) throws Exception {
        List<String> includeCategories = request.getIncludeCategories();
        if (includeCategories != null) {
            List<Integer> includeRules = new ArrayList<>();
            for (String msg : includeCategories) {
                includeRules.add(FlinkRule.getCodeByLangMsg(msg));
            }
            request.setDiagnosisRule(includeRules);
        }
        return flinkTaskDiagnosisService.pageJobs(request);
    }

    /**
     * get metadata list
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/pageMetadata")
    @ApiOperation(value = "page metadata", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<?> pageMetadata(@Validated @RequestBody DiagnosisAdviceListReq request) {
        try {
            FlinkTaskAppExample flinkTaskAppExample = new FlinkTaskAppExample();
            FlinkTaskAppExample.Criteria criteria = flinkTaskAppExample.createCriteria();
            if (request.getProjectName() != null && !request.getProjectName().equals("")) {
                criteria.andProjectNameEqualTo(request.getProjectName());
            }
            if (request.getFlowName() != null && !request.getFlowName().equals("")) {
                criteria.andFlowNameEqualTo(request.getFlowName());
            }
            if (request.getTaskName() != null && !request.getTaskName().equals("")) {
                criteria.andTaskNameEqualTo(request.getTaskName());
            }
            if (request.getJobName() != null && !request.getJobName().equals("")) {
                criteria.andJobNameEqualTo(request.getJobName());
            }
            if (request.getUsername() != null && !request.getUsername().equals("")) {
                criteria.andUsernameEqualTo(request.getUsername());
            }
            if (request.getTaskState() != null && !request.getTaskState().equals("")) {
                criteria.andTaskStateEqualTo(request.getTaskState());
            }
            PageHelper.startPage(request.getPage(), request.getPageSize());
            List<FlinkTaskApp> flinkTaskApps = flinkTaskAppMapper.selectByExample(flinkTaskAppExample);
            return CommonStatus.success(CommonPage.restPage(flinkTaskApps));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @PostMapping(value = "/deleteMetadata")
    @ApiOperation(value = "del metadata", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<?> deleteMetadata(@Validated @RequestBody FlinkTaskApp flinkTaskApp) {
        try {
            if (flinkTaskApp.getId() == null) {
                return CommonStatus.failed("id is empty");
            }
            flinkTaskAppMapper.deleteByPrimaryKey(flinkTaskApp.getId());
            return CommonStatus.success(flinkTaskApp);
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping("/orderMap")
    @ApiOperation(value = "order map")
    public CommonStatus<?> getOrderMap() {
        Map<String, String> orderMap = new HashMap<>();
        orderMap.put("tm_num * tm_mem - diagnosis_tm_num * diagnosis_tm_mem_size", MessageSourceUtil.get("OPTIMIZE_MEMORY"));
        orderMap.put("tm_num * tm_container_vcore_num - diagnosis_tm_num * diagnosis_tm_core_num", MessageSourceUtil.get("OPTIMIZE_CORE"));
        orderMap.put("parallel", MessageSourceUtil.get("TASK_PARALLEL"));
        orderMap.put("tm_mem", MessageSourceUtil.get("TASK_TM_MEM"));
        orderMap.put("tm_num * tm_mem", MessageSourceUtil.get("TASK_TOTAL_MEMORY"));
        orderMap.put("tm_slot_num", MessageSourceUtil.get("TASK_SLOT_AMOUNT"));
        orderMap.put("jm_mem", MessageSourceUtil.get("TASK_JM_MEMORY"));
        orderMap.put("tm_num * tm_container_vcore_num", MessageSourceUtil.get("TASK_TOTAL_CORE"));
        orderMap.put("diagnosis_tm_num * diagnosis_tm_core_num", MessageSourceUtil.get("TASK_ADVICE_CORE"));
        orderMap.put("diagnosis_parallel", MessageSourceUtil.get("TASK_ADVICE_PARALLEL"));
        orderMap.put("diagnosis_tm_mem_size", MessageSourceUtil.get("TASK_ADVICE_TM_MEM"));
        orderMap.put("diagnosis_tm_num * diagnosis_tm_mem_size", MessageSourceUtil.get("TASK_ADVICE_TOTAL_MEMORY"));
        orderMap.put("diagnosis_jm_mem_size", MessageSourceUtil.get("TASK_ADVICE_JM_MEMORY"));
        return CommonStatus.success(orderMap);
    }

    @GetMapping("/orderType")
    @ApiOperation(value = "sort type")
    public CommonStatus<?> getOrderType() {
        Map<String, String> orderMap = new HashMap<>();
        orderMap.put("desc", MessageSourceUtil.get("DESC"));
        orderMap.put("asc", MessageSourceUtil.get("ASC"));
        return CommonStatus.success(orderMap);
    }

    @GetMapping("/tableColumn")
    @ApiOperation(value = "get optional column of a table")
    public CommonStatus<?> getTableColumns() {
        Map<String, String> orderMap = new HashMap<>();
        orderMap.put("parallel", MessageSourceUtil.get("PARALLEL"));
        orderMap.put("tmSlot", MessageSourceUtil.get("TM_SLOT_AMOUNT"));
        orderMap.put("tmCore", MessageSourceUtil.get("TM_CORE_AMOUNT"));
        orderMap.put("tmMem", MessageSourceUtil.get("TM_MEM"));
        orderMap.put("jmMem", MessageSourceUtil.get("JM_MEM"));
        orderMap.put("tmNum", MessageSourceUtil.get("TM_NUM"));
        orderMap.put("diagnosisStartTime", MessageSourceUtil.get("DIAGNOSIS_START_TIME"));
        orderMap.put("diagnosisEndTime", MessageSourceUtil.get("DIAGNOSIS_END_TIME"));
        orderMap.put("diagnosisParallel", MessageSourceUtil.get("DIAGNOSIS_PARALLEL"));
        orderMap.put("diagnosisJmMemSize", MessageSourceUtil.get("DIAGNOSIS_JM_MEM_SIZE"));
        orderMap.put("diagnosisTmSlotNum", MessageSourceUtil.get("DIAGNOSIS_TM_SLOT_NUM"));
        orderMap.put("diagnosisTmCoreNum", MessageSourceUtil.get("DIAGNOSIS_TM_CORE_NUM"));
        orderMap.put("diagnosisTmNum", MessageSourceUtil.get("DIAGNOSIS_TM_NUM"));
        orderMap.put("metricJobName", MessageSourceUtil.get("METRIC_JOB_NAME"));
        orderMap.put("flinkTrackUrl", MessageSourceUtil.get("FLINK_TRACK_URL"));
        orderMap.put("applicationId", MessageSourceUtil.get("APPLICATION_ID"));
        return CommonStatus.success(orderMap);
    }

    @PostMapping("/getReport")
    @ApiOperation(value = "get the report of diagnosis")
    public CommonStatus<?> getReport(@Validated @RequestBody ReportDetailReq request) throws Exception {
        return CommonStatus.success(flinkTaskDiagnosisService.getReport(request));
    }

    @PostMapping("/getGeneralViewNumber")
    @ApiOperation(value = "get general view")
    public CommonStatus<?> getGeneralViewNumber(@Validated @RequestBody DiagnosisGeneralViewReq request) throws Exception {
        DiagnosisGeneralViewNumberResp generalViewNumber = flinkTaskDiagnosisService.getGeneralViewNumber(request);
        return CommonStatus.success(generalViewNumber);
    }

    /**
     * Get memory trend,CPu trend,number trend
     *
     * @param request
     * @return
     */
    @PostMapping("/getGeneralViewTrend")
    @ApiOperation(value = "get trend statistic")
    public CommonStatus<?> getGeneralViewTrend(@Validated @RequestBody DiagnosisGeneralViewReq request) throws Exception {
        DiagnosisGeneralViewTrendResp generalViewTrend = flinkTaskDiagnosisService.getGeneralViewTrend(request);
        if (generalViewTrend != null) {
            return CommonStatus.success(generalViewTrend);
        } else {
            return CommonStatus.failed(MessageSourceUtil.get("NO_DATA_PERIOD"));
        }
    }

    @PostMapping("/getGeneralViewDistribute")
    @ApiOperation(value = "get general view distribution")
    public CommonStatus<?> getGeneralViewDistribute(@Validated @RequestBody DiagnosisGeneralViewReq request) throws Exception {
        DiagnosisGeneralVIewDistributeResp generalViewDistribute = flinkTaskDiagnosisService.getGeneralViewDistribute(request);
        return CommonStatus.success(generalViewDistribute);
    }

    @PostMapping("/diagnosis")
    @ApiOperation(value = "in-time diagnosis")
    public CommonStatus<?> diagnosis(@RequestBody OneClickDiagnosisRequest req) throws Exception {
        req.setAppId(req.getAppId().trim().replace("\t", ""));
        FlinkTaskAppExample flinkTaskAppExample = new FlinkTaskAppExample();
        flinkTaskAppExample.createCriteria()
                .andApplicationIdEqualTo(req.getAppId());
        Optional<FlinkTaskApp> task = flinkTaskAppMapper.selectByExample(flinkTaskAppExample)
                .stream()
                .max(Comparator.comparing(FlinkTaskApp::getStartTime));
        if (task.isPresent()) {
            // block list check
            BlocklistExample blocklistExample = new BlocklistExample();
            BlocklistExample.Criteria criteria = blocklistExample.createCriteria()
                    .andTaskNameEqualTo(task.get().getTaskName())
                    .andFlowNameEqualTo(task.get().getFlowName())
                    .andProjectNameEqualTo(task.get().getProjectName())
                    .andComponentEqualTo(ComponentEnum.Realtime.getDes())
                    .andDeletedEqualTo(0);
            List<Blocklist> blockLists = blocklistMapper.selectByExample(blocklistExample);
            log.debug(blocklistExample.getOredCriteria().toString());
            if (blockLists != null && blockLists.size() > 0) {
                return CommonStatus.failed("The task is in the blocklist");
            }
            Long endTime = req.getEnd();
            Long startTime = req.getStart();
            if (req.getStart() == null || req.getEnd() == null) {
                endTime = LocalDateTime.now(ZoneOffset.ofHours(8)).toEpochSecond(ZoneOffset.ofHours(8));
                startTime = LocalDateTime.now(ZoneOffset.ofHours(8)).minusDays(1).toEpochSecond(ZoneOffset.ofHours(8));
            }
            FlinkTaskAnalysis flinkTaskAnalysis = diagnosisService.diagnosisApp(task.get(),
                    startTime, endTime, DiagnosisFrom.Manual);
            if (flinkTaskAnalysis == null) {
                return CommonStatus.failed("failed to diagnosis");
            }
            OneClickDiagnosisResponse res = new OneClickDiagnosisResponse();
            res.setFlinkTaskAnalysis(flinkTaskAnalysis);
            res.setStatus("succeed");
            return CommonStatus.success(res);
        } else {
            return CommonStatus.failed(String.format("task not found: %s", req.getAppId()));
        }
    }
}


