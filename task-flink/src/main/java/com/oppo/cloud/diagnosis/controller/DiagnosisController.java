package com.oppo.cloud.diagnosis.controller;

import com.oppo.cloud.diagnosis.domain.dto.DiagnosisRequest;
import com.oppo.cloud.diagnosis.domain.dto.OResult;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.diagnosis.service.DiagnosisService;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.RealtimeTaskApp;
import com.oppo.cloud.model.RealtimeTaskAppExample;
import com.oppo.cloud.model.RealtimeTaskDiagnosis;
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
                LocalDateTime endTime = req.getEnd();
                LocalDateTime startTime = req.getStart();
                if (req.getStart() == null || req.getEnd() == null) {
                    endTime = LocalDateTime.now(ZoneOffset.ofHours(8));
                    startTime = endTime.minusDays(1);
                }
                long endSec = endTime.toEpochSecond(ZoneOffset.ofHours(8));
                long startSec = startTime.toEpochSecond(ZoneOffset.ofHours(8));
                RealtimeTaskDiagnosis realtimeTaskDiagnosis = diagnosisService.diagnosisApp(task.get(),
                        startSec, endSec, DiagnosisFrom.Manual);
                return OResult.success(realtimeTaskDiagnosis);
            } else {
                return OResult.fail(String.format("没有找到该任务:%s", req));
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return OResult.fail(t.getMessage());
        }
    }

}
