package com.oppo.cloud.diagnosis.task;

import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.diagnosis.service.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
public class DiagnosisTask {
    @Autowired
    DiagnosisService diagnosisService;

    /**
     * 每日定时诊断
     */
    @Scheduled(cron = "1 */10 * * * ?")
    public void diagnosis() {
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
