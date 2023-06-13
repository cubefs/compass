package com.oppo.cloud.diagnosis.controller;

import com.oppo.cloud.common.constant.ComponentEnum;
import com.oppo.cloud.diagnosis.domain.dto.DiagnosisRequest;
import com.oppo.cloud.diagnosis.domain.dto.OResult;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.diagnosis.service.DiagnosisService;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ostream")
@Slf4j
@Api(tags = "diagnosis")
public class DiagnosisController {
    @Autowired
    DiagnosisService diagnosisService;
    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;

    @Autowired
    BlocklistMapper blocklistMapper;

    @PostMapping("/diagnosis")
    @ApiOperation(value = "诊断")
    public OResult<RealtimeTaskDiagnosis> diagnosis(@RequestBody DiagnosisRequest req) {
        try {
            RealtimeTaskAppExample realtimeTaskAppExample = new RealtimeTaskAppExample();
            realtimeTaskAppExample.createCriteria()
                    .andApplicationIdEqualTo(req.getAppId());
            Optional<RealtimeTaskApp> task = flinkTaskAppMapper.selectByExample(realtimeTaskAppExample)
                    .stream()
                    .max(Comparator.comparing(RealtimeTaskApp::getStartTime));
            if (task.isPresent()) {
                // 黑名单检查
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
                    log.debug("白名单拦截:{}", task.get());
                    return OResult.fail("该任务在白名单中");
                } else {
                    log.debug("白名单通过:{}", task.get());
                }
                Long endTime = req.getEnd();
                Long startTime = req.getStart();
                if (req.getStart() == null || req.getEnd() == null) {
                    endTime = LocalDateTime.now(ZoneOffset.ofHours(8)).toEpochSecond(ZoneOffset.ofHours(8));
                    startTime = LocalDateTime.now(ZoneOffset.ofHours(8)).minusDays(1).toEpochSecond(ZoneOffset.ofHours(8));
                }
                RealtimeTaskDiagnosis realtimeTaskDiagnosis = diagnosisService.diagnosisApp(task.get(),
                        startTime, endTime, DiagnosisFrom.Manual);
                if (realtimeTaskDiagnosis == null) {
                    return OResult.fail("诊断失败");
                }
                return OResult.success(realtimeTaskDiagnosis);
            } else {
                return OResult.fail(String.format("没有找到该任务:%s", req.getAppId()));
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return OResult.fail(t.getMessage());
        }
    }
}
