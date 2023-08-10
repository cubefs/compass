package com.oppo.cloud.portal.controller;

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.flink.service.DiagnosisService;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskAppExample;
import com.oppo.cloud.portal.service.FlinkTaskDiagnosisService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Flink HTTP API 接口上报元数据
 */
@Slf4j
@Controller
public class FlinkMetadataController {

    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;

    @PostMapping("/openapi/flink/metadatas")
    @ApiOperation(value = "诊断")
    public CommonStatus<FlinkTaskApp> saveRealtimeTaskApp(@RequestBody FlinkTaskApp flinkTaskApp) {
        try {
            FlinkTaskAppExample flinkTaskAppExample = new FlinkTaskAppExample();
            flinkTaskAppExample.createCriteria()
                    .andApplicationIdEqualTo(flinkTaskApp.getApplicationId());
            List<FlinkTaskApp> flinkTaskApps = flinkTaskAppMapper.selectByExample(flinkTaskAppExample);
            if (flinkTaskApps == null || flinkTaskApps.size() == 0) {
                flinkTaskAppMapper.insert(flinkTaskApp);
                return CommonStatus.success(flinkTaskApp);
            } else if (flinkTaskApps.size() == 1) {
                FlinkTaskApp pre = flinkTaskApps.get(0);
                pre.setTaskState(flinkTaskApp.getTaskState());
                flinkTaskAppMapper.updateByPrimaryKeySelective(pre);
                return CommonStatus.success(pre);
            } else {
                log.error("realtimeTaskApps size 大于1 , appid:{}", flinkTaskApp.getApplicationId());
                return CommonStatus.failed("内部错误");
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("内部错误");
        }
    }
}
