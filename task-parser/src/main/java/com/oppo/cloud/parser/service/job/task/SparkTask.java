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

package com.oppo.cloud.parser.service.job.task;

import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.config.MemWasteConfig;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.util.spring.SpringBeanUtil;
import com.oppo.cloud.parser.config.ThreadPoolConfig;
import com.oppo.cloud.parser.domain.job.*;
import com.oppo.cloud.parser.service.rules.JobRulesConfigService;
import com.oppo.cloud.parser.service.job.detector.spark.MemWasteDetector;
import com.oppo.cloud.parser.service.job.parser.IParser;
import com.oppo.cloud.parser.service.writer.OpenSearchWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Spark task
 */
@Slf4j
public class SparkTask extends Task {

    private final TaskParam taskParam;
    private final MemWasteConfig memWasteConfig;
    private final Executor taskThreadPool;

    public SparkTask(TaskParam taskParam) {
        super(taskParam);
        this.taskParam = taskParam;
        taskThreadPool = (ThreadPoolTaskExecutor) SpringBeanUtil.getBean(ThreadPoolConfig.TASK_THREAD_POOL);
        JobRulesConfigService jobRulesConfigService = (JobRulesConfigService) SpringBeanUtil.getBean(JobRulesConfigService.class);
        this.memWasteConfig = jobRulesConfigService.detectorConfig.getMemWasteConfig();
    }

    @Override
    public TaskResult run() {
        List<IParser> parsers = super.createTasks();
        if (parsers.size() == 0) {
            return null;
        }
        List<CompletableFuture<CommonResult>> futures = super.createFutures(parsers, taskThreadPool);

        TaskResult taskResult = new TaskResult();
        taskResult.setAppId(this.taskParam.getApp().getAppId());

        List<GCReport> gcReports = new ArrayList<>();
        List<SparkExecutorLogParserResult> sparkExecutorLogParserResults = null;
        SparkEventLogParserResult sparkEventLogParserResult = null;

        for (Future<CommonResult> result : futures) {
            CommonResult commonResult;
            try {
                commonResult = result.get();
                if (commonResult != null) {
                    switch (commonResult.getLogType()) {
                        case SPARK_DRIVER:
                        case SPARK_EXECUTOR:
                            sparkExecutorLogParserResults =
                                    (List<SparkExecutorLogParserResult>) commonResult.getResult();
                            break;
                        case SPARK_EVENT:
                            sparkEventLogParserResult = (SparkEventLogParserResult) commonResult.getResult();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("Exception:{}", e);
            }
        }

        if (sparkEventLogParserResult == null) {
            log.error("sparkEventLogParserResultNull:{}", taskParam.getApp());
            return null;
        }
        // get driver/executor categories
        List<String> executorCategories = new ArrayList<>();
        if (sparkExecutorLogParserResults != null) {
            for (SparkExecutorLogParserResult result : sparkExecutorLogParserResults) {
                if (result.getGcReports() != null) {
                    gcReports.addAll(result.getGcReports());
                }
                if (result.getCategories() != null) {
                    executorCategories.addAll(result.getCategories());
                }
            }
        }
        taskResult.setCategories(executorCategories);

        DetectorStorage detectorStorage = sparkEventLogParserResult.getDetectorStorage();
        if (detectorStorage == null) {
            log.error("detectorStorageNull:{}", taskParam.getApp());
            return taskResult;
        }
        // calculate memory metrics
        if (!this.memWasteConfig.getDisable() && gcReports.size() > 0
                && sparkEventLogParserResult.getMemoryCalculateParam() != null) {
            MemWasteDetector memWasteDetector = new MemWasteDetector(this.memWasteConfig);
            DetectorResult detectorResult =
                    memWasteDetector.detect(gcReports, sparkEventLogParserResult.getMemoryCalculateParam());
            detectorStorage.addDetectorResult(detectorResult);
            if (detectorResult.getAbnormal()) {
                detectorStorage.setAbnormal(true);
            }
        }
        // get event log categories
        List<String> eventLogCategories = new ArrayList<>();
        if (this.taskParam.getLogRecord().getIsOneClick() || detectorStorage.getAbnormal()) {
            for (DetectorResult detectorResult : detectorStorage.getDataList()) {
                if (detectorResult.getAbnormal()) {
                    eventLogCategories.add(detectorResult.getAppCategory());
                }
            }
            // save all detector results
            OpenSearchWriter.getInstance().saveDetectorStorage(detectorStorage);
        } else {
            // save event log env
            detectorStorage.setDataList(null);
            OpenSearchWriter.getInstance().saveDetectorStorage(detectorStorage);
        }
        eventLogCategories.addAll(executorCategories);
        // set all spark categories
        taskResult.setCategories(eventLogCategories);

        // save gc reports
        if ((this.taskParam.getLogRecord().getIsOneClick() || eventLogCategories.size() > 0) && gcReports.size() > 0) {
            gcReports.sort(Comparator.comparing(GCReport::getMaxHeapUsedSize));
            if (gcReports.size() > 11) {
                List<GCReport> results = new ArrayList<>();
                GCReport driverGc = gcReports.stream().filter(gc -> gc.getExecutorId() == 0).findFirst().orElse(null);
                List<GCReport> executorGcs = gcReports.subList(gcReports.size() - 10, gcReports.size());
                if (driverGc != null) {
                    results.add(driverGc);
                }
                results.addAll(executorGcs);
                OpenSearchWriter.getInstance().saveGCReports(results, detectorStorage.getExecutionTime(),
                        detectorStorage.getApplicationId());
            } else {
                OpenSearchWriter.getInstance().saveGCReports(gcReports, detectorStorage.getExecutionTime(),
                        detectorStorage.getApplicationId());
            }
        }

        return taskResult;
    }
}
