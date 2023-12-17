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

package com.oppo.cloud.parser.service.job.parser;

import com.oppo.cloud.common.constant.ApplicationType;
import com.oppo.cloud.common.constant.ProgressState;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.eventlog.config.SparkEnvironmentConfig;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.parser.domain.job.*;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkApplication;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkExecutor;
import com.oppo.cloud.parser.service.job.detector.DetectorManager;
import com.oppo.cloud.parser.service.reader.IReader;
import com.oppo.cloud.parser.service.reader.LogReaderFactory;
import com.oppo.cloud.parser.utils.ReplaySparkEventLogs;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SparkEventLogParser extends IParser {

    private DetectorConfig config;

    public SparkEventLogParser(ParserParam param, DetectorConfig config) {
        super(param);
        this.config = config;
    }

    @Override
    public CommonResult run() {
        updateParserProgress(ProgressState.PROCESSING, 0, this.param.getLogPaths().size());
        if (this.param.getLogPaths().size() > 0) {
            LogPath logPath = this.param.getLogPaths().get(0);
            ReaderObject readerObject;
            try {
                IReader reader = LogReaderFactory.create(logPath);
                readerObject = reader.getReaderObject();
            } catch (FileNotFoundException e) {
                String path = logPath.getLogPath().substring(0, logPath.getLogPath().lastIndexOf("_"));
                logPath.setLogPath(path);
                try {
                    readerObject = LogReaderFactory.create(logPath).getReaderObject();
                } catch (Exception ex) {
                    log.error("Exception:", e);
                    updateParserProgress(ProgressState.FAILED, 0, 0);
                    return null;
                }
            } catch (Exception e) {
                log.error("Exception:", e);
                updateParserProgress(ProgressState.FAILED, 0, 0);
                return null;
            }
            return parse(readerObject);
        }
        return null;
    }

    private CommonResult<SparkEventLogParserResult> parse(ReaderObject readerObject) {
        ReplaySparkEventLogs replayEventLogs = new ReplaySparkEventLogs();
        try {
            replayEventLogs.replay(readerObject);
        } catch (Exception e) {
            log.error("Exception:", e);
            updateParserProgress(ProgressState.FAILED, 0, 0);
            return null;
        } finally {
            readerObject.close();
        }
        return detect(replayEventLogs, readerObject.getLogPath());
    }

    private CommonResult<SparkEventLogParserResult> detect(ReplaySparkEventLogs replayEventLogs, String logPath) {
        Map<String, Object> env = getSparkEnvironmentConfig(replayEventLogs);

        Long appDuration = replayEventLogs.getApplication().getAppDuration();
        if (appDuration == null || appDuration < 0) {
            appDuration = 0L;
        }

        DetectorParam detectorParam = new DetectorParam(this.param.getLogRecord().getJobAnalysis().getFlowName(),
                this.param.getLogRecord().getJobAnalysis().getProjectName(),
                this.param.getLogRecord().getJobAnalysis().getTaskName(),
                this.param.getLogRecord().getJobAnalysis().getExecutionDate(),
                this.param.getLogRecord().getJobAnalysis().getRetryTimes(),
                this.param.getApp().getAppId(), ApplicationType.SPARK, appDuration, logPath, config,
                this.param.getLogRecord().getIsOneClick());
        detectorParam.setReplayEventLogs(replayEventLogs);

        DetectorManager detectorManager = new DetectorManager();
        // run all detector
        DetectorStorage detectorStorage = detectorManager.run(detectorParam);

        detectorStorage.setEnv(env);
        SparkEventLogParserResult sparkEventLogParserResult = new SparkEventLogParserResult();
        sparkEventLogParserResult.setDetectorStorage(detectorStorage);
        sparkEventLogParserResult.setMemoryCalculateParam(getMemoryCalculateParam(replayEventLogs));

        CommonResult<SparkEventLogParserResult> result = new CommonResult<>();
        result.setLogType(this.param.getLogType());
        result.setResult(sparkEventLogParserResult);

        updateParserProgress(ProgressState.SUCCEED, 0, 0);
        return result;
    }

    private Map<String, Object> getSparkEnvironmentConfig(ReplaySparkEventLogs replayEventLogs) {
        Map<String, Object> env = new HashMap<>();
        SparkEnvironmentConfig envConfig = config.getSparkEnvironmentConfig();
        if (envConfig != null) {
            if (envConfig.getJvmInformation() != null) {
                for (String key : envConfig.getJvmInformation()) {
                    env.put(key, replayEventLogs.getApplication().getJvmInformation().get(key));
                }
            }
            if (envConfig.getSparkProperties() != null) {
                for (String key : envConfig.getSparkProperties()) {
                    env.put(key, replayEventLogs.getApplication().getSparkProperties().get(key));
                }
            }
            if (envConfig.getSystemProperties() != null) {
                for (String key : envConfig.getSystemProperties()) {
                    env.put(key, replayEventLogs.getApplication().getSystemProperties().get(key));
                }
            }
        }
        return env;
    }


    public MemoryCalculateParam getMemoryCalculateParam(ReplaySparkEventLogs replayEventLogs) {
        SparkApplication application = replayEventLogs.getApplication();
        long appTotalTime = application.getAppEndTimestamp() - application.getAppStartTimestamp();
        MemoryCalculateParam memoryCalculateParam = new MemoryCalculateParam();
        memoryCalculateParam.setAppTotalTime(appTotalTime > 0 ? appTotalTime : 0);
        memoryCalculateParam.setDriverMemory(application.getDriverMemory());
        memoryCalculateParam.setExecutorMemory(application.getExecutorMemory());

        Map<String, Long> executorRuntimeMap = new HashMap<>();
        for (Map.Entry<String, SparkExecutor> executor : replayEventLogs.getExecutors().entrySet()) {
            SparkExecutor sparkExecutor = executor.getValue();
            long endTime = sparkExecutor.getRemoveTimestamp() > 0 ? sparkExecutor.getRemoveTimestamp()
                    : application.getAppEndTimestamp();

            long startTime = sparkExecutor.getStartTimestamp() > 0 ? sparkExecutor.getStartTimestamp()
                    : application.getAppStartTimestamp();

            long executorRuntime = endTime - startTime;
            executorRuntimeMap.put(executor.getValue().getId(), executorRuntime);
        }
        memoryCalculateParam.setExecutorRuntimeMap(executorRuntimeMap);
        return memoryCalculateParam;
    }

}
