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

package com.oppo.cloud.flink.detect;

import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.flink.service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
public class TaskDetectScheduler {
    @Autowired
    DiagnosisService diagnosisService;

    /**
     * TODO: 自定义时间诊断
     */
    public void CustomDetect() {
    }

    /**
     * 每日定时诊断
     */
    @Scheduled(cron = "1 */10 * * * ?")
    public void DetectDaily() {
        log.info("开始执行定时诊断任务");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(8));
        LocalDateTime endDate = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());
        long end = endDate.toEpochSecond(ZoneOffset.ofHours(8));
        LocalDateTime startDate = endDate.plusDays(-1);
        long start = startDate.toEpochSecond(ZoneOffset.ofHours(8));
        diagnosisService.diagnosisAllApp(start, end, DiagnosisFrom.EveryDay);
        log.info("结束执行定时诊断任务");
    }

    /**
     * 小时级别定时诊断
     */
    @Scheduled(cron = "1 0 * * * ?")
    public void diagnosisHourly() {
        log.info("开始执行定时诊断任务");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.ofHours(8));
        LocalDateTime endDate = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute(), now.getSecond());
        long end = endDate.toEpochSecond(ZoneOffset.ofHours(8));
        LocalDateTime startDate = endDate.plusDays(-1);
        long start = startDate.toEpochSecond(ZoneOffset.ofHours(8));
        diagnosisService.diagnosisAppHourly(start, end, DiagnosisFrom.JobUptime);
        log.info("结束执行定时诊断任务");
    }
}
