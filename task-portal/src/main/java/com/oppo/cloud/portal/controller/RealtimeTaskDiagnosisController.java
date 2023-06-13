package com.oppo.cloud.portal.controller;


import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.api.CommonPage;
import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisResourceType;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisRuleType;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.RealtimeTaskApp;
import com.oppo.cloud.model.RealtimeTaskAppExample;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;
import com.oppo.cloud.portal.domain.realtime.*;
import com.oppo.cloud.portal.service.FlinkTaskDiagnosisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/realtime/taskDiagnosis")
@Api(value = "RealtimeTaskDiagnosisController", description = "实时任务诊断查询接口")
@Slf4j
public class RealtimeTaskDiagnosisController {

    @Autowired
    FlinkTaskDiagnosisService realtimeService;
    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;

    @GetMapping(value = "/diagnosisRules")
    @ApiOperation(value = "获取诊断规则信息")
    public CommonStatus<List<DiagnosisRuleResp>> getDiagnosisRules() {
        FlinkRule[] flinkRules = FlinkRule.values();
        List<DiagnosisRuleResp> list = new ArrayList<>();
        for (FlinkRule ediagnosisRule : flinkRules) {
            DiagnosisRuleResp diagnosisRuleResp = new DiagnosisRuleResp();
            diagnosisRuleResp.setCode(ediagnosisRule.getCode());
            diagnosisRuleResp.setName(ediagnosisRule.getName());
            diagnosisRuleResp.setDesc(ediagnosisRule.getDesc());
            list.add(diagnosisRuleResp);
        }
        return CommonStatus.success(list);
    }

    @GetMapping(value = "/diagnosisFrom")
    @ApiOperation(value = "获取诊断来源信息")
    public CommonStatus<List<DiagnosisFromResp>> getDiagnosisFromTypes() {
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
    @ApiOperation(value = "获取资源诊断规则信息")
    public CommonStatus<List<DiagnosisRuleResp>> getResourceDiagnosisRules() {
        FlinkRule[] flinkRules = FlinkRule.values();
        List<DiagnosisRuleResp> list = new ArrayList<>();
        for (FlinkRule ediagnosisRule : flinkRules) {
            if (ediagnosisRule.getRuleType() == DiagnosisRuleType.ResourceRule.getCode()) {
                DiagnosisRuleResp diagnosisRuleResp = new DiagnosisRuleResp();
                diagnosisRuleResp.setCode(ediagnosisRule.getCode());
                diagnosisRuleResp.setName(ediagnosisRule.getName());
                diagnosisRuleResp.setDesc(ediagnosisRule.getDesc());
                list.add(diagnosisRuleResp);
            }
        }
        return CommonStatus.success(list);
    }

    @GetMapping(value = "/runtimeExceptionDiagnosisTypes")
    @ApiOperation(value = "获取运行异常诊断信息")
    public CommonStatus<List<DiagnosisRuleResp>> getRuntimeExceptionDiagnosisRules() {
        FlinkRule[] flinkRules = FlinkRule.values();
        List<DiagnosisRuleResp> list = new ArrayList<>();
        for (FlinkRule ediagnosisRule : flinkRules) {
            if (ediagnosisRule.getRuleType() == DiagnosisRuleType.RuntimeExceptionRule.getCode()) {
                DiagnosisRuleResp diagnosisRuleResp = new DiagnosisRuleResp();
                diagnosisRuleResp.setCode(ediagnosisRule.getCode());
                diagnosisRuleResp.setName(ediagnosisRule.getName());
                diagnosisRuleResp.setDesc(ediagnosisRule.getDesc());
                list.add(diagnosisRuleResp);
            }
        }
        return CommonStatus.success(list);
    }

    @GetMapping(value = "/resourceDiagnosisTypes")
    @ApiOperation(value = "获取诊断类别信息")
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
     * 获取Job列表
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/page")
    @ApiOperation(value = "诊断作业列表", httpMethod = "POST")
    @ResponseBody
    public CommonStatus<CommonPage<RealtimeTaskDiagnosis>> pageJobs(@Validated @RequestBody DiagnosisAdviceListReq request) {
        try {
            List<String> includeCategories = request.getIncludeCategories();
            if (includeCategories != null) {
                List<Integer> includeRules = new ArrayList<>();
                for (String msg : includeCategories) {
                    for (FlinkRule fr : FlinkRule.values()) {
                        if (fr.getName().equals(msg)) {
                            includeRules.add(fr.getCode());
                        }
                    }
                }
                request.setDiagnosisRule(includeRules);
            }
            return CommonStatus.success(realtimeService.pageJobs(request));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }

    @GetMapping("/orderMap")
    @ApiOperation(value = "排序列")
    public CommonStatus<Map<String, String>> getOrderMap() {
        Map<String, String> orderMap = new HashMap<>();
        orderMap.put("tm_num * tm_mem - diagnosis_tm_num * diagnosis_tm_mem_size", "任务可优化总内存数");
        orderMap.put("tm_num * tm_container_vcore_num - diagnosis_tm_num * diagnosis_tm_core_num", "任务可优化总核数");
        orderMap.put("parallel", "任务并行度");
        orderMap.put("tm_mem", "任务tm内存");
        orderMap.put("tm_num * tm_mem", "任务总内存数");
        orderMap.put("tm_slot_num", "任务slot数量");
        orderMap.put("jm_mem", "任务jm内存数");
        orderMap.put("tm_num * tm_container_vcore_num", "任务总core数");
        orderMap.put("diagnosis_tm_num * diagnosis_tm_core_num", "任务建议总core数");
        orderMap.put("diagnosis_parallel", "任务建议并行度");
        orderMap.put("diagnosis_tm_mem_size", "任务建议tm内存");
        orderMap.put("diagnosis_tm_num * diagnosis_tm_mem_size", "任务建议总内存数");
        orderMap.put("diagnosis_jm_mem_size", "任务建议jm内存");
        return CommonStatus.success(orderMap);
    }

    @GetMapping("/orderType")
    @ApiOperation(value = "排序方式")
    public CommonStatus<Map<String, String>> getOrderType() {
        Map<String, String> orderMap = new HashMap<>();
        orderMap.put("desc", "倒序");
        orderMap.put("asc", "正序");
        return CommonStatus.success(orderMap);
    }

    @GetMapping("/tableColumn")
    @ApiOperation(value = "获取table展示的可选列")
    public CommonStatus<Map<String, String>> getTableColumns() {
        Map<String, String> orderMap = new HashMap<>();
        orderMap.put("parallel", "并行度");
        orderMap.put("tmSlot", "TM的slot数量");
        orderMap.put("tmCore", "TM的core数量");
        orderMap.put("tmMem", "tm内存MB");
        orderMap.put("jmMem", "jm内存MB");
        orderMap.put("tmNum", "tm数量");
        orderMap.put("diagnosisStartTime", "诊断起始时间");
        orderMap.put("diagnosisEndTime", "诊断结束时间");
        orderMap.put("diagnosisParallel", "建议并行度");
        orderMap.put("diagnosisJmMemSize", "建议jm内存MB");
        orderMap.put("diagnosisTmSlotNum", "建议TM的slot数量");
        orderMap.put("diagnosisTmCoreNum", "建议tm的core数量");
        orderMap.put("diagnosisTmNum", "建议tm数量");
        orderMap.put("metricJobName", "上报metric的job名字");
        orderMap.put("flinkTrackUrl", "Flink track url");
        orderMap.put("applicationId", "Application ID");
        return CommonStatus.success(orderMap);
    }

    @PostMapping("/getReport")
    @ApiOperation(value = "获取诊断报告")
    public CommonStatus<DiagnosisReportResp> getReport(@Validated @RequestBody RealtimeTaskDiagnosis request) {
        try {
            return CommonStatus.success(realtimeService.getReport(request));
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误");
        }
    }

    @PostMapping("/getGeneralViewNumber")
    @ApiOperation(value = "获取概览数值指标")
    public CommonStatus<DiagnosisGeneralViewNumberResp> getGeneralViewNumber(@Validated @RequestBody DiagnosisGeneralViewReq request) {
        try {
            DiagnosisGeneralViewNumberResp generalViewNumber = realtimeService.getGeneralViewNumber(request);
            return CommonStatus.success(generalViewNumber);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误请查看日志");
        }
    }

    @PostMapping("/getGeneralViewTrend")
    @ApiOperation(value = "获取概览趋势指标")
    public CommonStatus<DiagnosisGeneralViewTrendResp> getGeneralViewTrend(@Validated @RequestBody DiagnosisGeneralViewReq request) {
        try {
            DiagnosisGeneralViewTrendResp generalViewTrend = realtimeService.getGeneralViewTrend(request);
            if (generalViewTrend != null) {
                return CommonStatus.success(generalViewTrend);
            } else {
                return CommonStatus.failed("该周期内无数据");
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误请查看日志");
        }
    }

    @PostMapping("/getGeneralViewDistribute")
    @ApiOperation(value = "获取概览分布指标")
    public CommonStatus<DiagnosisGeneralVIewDistributeResp> getGeneralViewDistribute(@Validated @RequestBody DiagnosisGeneralViewReq request) {
        try {
            DiagnosisGeneralVIewDistributeResp generalViewDistribute = realtimeService.getGeneralViewDistribute(request);
            return CommonStatus.success(generalViewDistribute);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误请查看日志");
        }
    }

    @PostMapping("/updateState")
    @ApiOperation(value = "更新状态")
    public CommonStatus<RealtimeTaskDiagnosis> updateState(@Validated @RequestBody RealtimeTaskDiagnosis realtimeTaskDiagnosis) {
        try {
            return realtimeService.updateStatus(realtimeTaskDiagnosis);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误");
        }
    }

    @PostMapping("/diagnosis")
    @ApiOperation(value = "诊断")
    public CommonStatus<OneClickDiagnosisResponse> diagnosis(@RequestBody OneClickDiagnosisRequest req) {
        try {
            req.setAppId(req.getAppId().trim().replace("\t", ""));
            return realtimeService.diagnosis(req);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return CommonStatus.failed("未知错误");
    }

    @PostMapping("/saveRealtimeTaskApp")
    @ApiOperation(value = "诊断")
    public CommonStatus<RealtimeTaskApp> saveRealtimeTaskApp(@RequestBody RealtimeTaskApp realtimeTaskApp) {
        try {
            RealtimeTaskAppExample realtimeTaskAppExample = new RealtimeTaskAppExample();
            realtimeTaskAppExample.createCriteria()
                    .andApplicationIdEqualTo(realtimeTaskApp.getApplicationId());
            List<RealtimeTaskApp> realtimeTaskApps = flinkTaskAppMapper.selectByExample(realtimeTaskAppExample);
            if (realtimeTaskApps == null || realtimeTaskApps.size() == 0) {
                flinkTaskAppMapper.insert(realtimeTaskApp);
                return CommonStatus.success(realtimeTaskApp);
            } else if (realtimeTaskApps.size() == 1) {
                RealtimeTaskApp pre = realtimeTaskApps.get(0);
                pre.setTaskState(realtimeTaskApp.getTaskState());
                flinkTaskAppMapper.updateByPrimaryKeySelective(pre);
                return CommonStatus.success(pre);
            } else {
                log.error("realtimeTaskApps size 大于1 , appid:{}", realtimeTaskApp.getApplicationId());
                return CommonStatus.failed("内部错误");
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误");
        }
    }
}


