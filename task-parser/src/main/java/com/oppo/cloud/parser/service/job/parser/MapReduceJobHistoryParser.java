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
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.mr.config.MREnvironmentConfig;
import com.oppo.cloud.common.domain.oneclick.OneClickProgress;
import com.oppo.cloud.common.domain.oneclick.ProgressInfo;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.service.job.detector.DetectorManager;
import com.oppo.cloud.parser.service.reader.IReader;
import com.oppo.cloud.parser.service.reader.LogReaderFactory;
import com.oppo.cloud.parser.utils.JobHistoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MapReduceJobHistoryParser extends IParser {

    private final ParserParam param;

    private DetectorConfig config;

    private boolean isOneClick;

    public MapReduceJobHistoryParser(ParserParam param, DetectorConfig config) {
        this.param = param;
        this.config = config;
        this.isOneClick = param.getLogRecord().getIsOneClick();
    }

    @Override
    public CommonResult run() {
        updateParserProgress(ProgressState.PROCESSING, 0, this.param.getLogPaths().size());
        if (this.param.getLogPaths() == null) {
            return null;
        }
        List<ReaderObject> readerObjects = null;
        for (LogPath logPath : this.param.getLogPaths()) {
            List<ReaderObject> results;
            try {
                IReader reader = LogReaderFactory.create(logPath);
                results = reader.getReaderObjects();
            } catch (Exception e) {
                log.error("Exception: ", e);
                continue;
            }
            if (results != null && results.size() > 0) {
                readerObjects = results;
                break;
            }
        }
        if (readerObjects == null) {
            updateParserProgress(ProgressState.FAILED, 0, 0);
            return null;
        }
        return parse(readerObjects);
    }

    private CommonResult parse(List<ReaderObject> readerObjects) {
        MRAppInfo mrAppInfo;
        try {
            mrAppInfo = JobHistoryUtil.parseJobHistory(readerObjects);
        } catch (Exception e) {
            log.error("Exception:", e);
            updateParserProgress(ProgressState.FAILED, 0, 0);
            return null;
        }  finally {
            readerObjects.forEach(ReaderObject::close);
        }
        return detect(mrAppInfo);
    }

    private CommonResult detect(MRAppInfo mrAppInfo) {

        long appDuration = mrAppInfo.getElapsedTime();

        DetectorParam detectorParam = new DetectorParam(this.param.getLogRecord().getJobAnalysis().getFlowName(),
                this.param.getLogRecord().getJobAnalysis().getProjectName(),
                this.param.getLogRecord().getJobAnalysis().getTaskName(),
                this.param.getLogRecord().getJobAnalysis().getExecutionDate(),
                this.param.getLogRecord().getJobAnalysis().getRetryTimes(),
                this.param.getApp().getAppId(), ApplicationType.MAPREDUCE, appDuration, "", config,
                this.param.getLogRecord().getIsOneClick());
        detectorParam.setMrAppInfo(mrAppInfo);

        DetectorManager detectorManager = new DetectorManager();
        // run all detector
        DetectorStorage detectorStorage = detectorManager.run(detectorParam);

        detectorStorage.setEnv(getMREnvironmentConfig(mrAppInfo));

        CommonResult result = new CommonResult<>();
        result.setLogType(this.param.getLogType());
        result.setResult(detectorStorage);

        updateParserProgress(ProgressState.SUCCEED, 0, 0);
        return result;
    }

    private Map<String, Object> getMREnvironmentConfig(MRAppInfo mrAppInfo) {
        Map<String, Object> env = new HashMap<>();
        MREnvironmentConfig envConfig = config.getMrEnvironmentConfig();
        if (envConfig != null && envConfig.getKeys() != null) {
            for (String key : envConfig.getKeys()) {
                env.put(key, mrAppInfo.getConfMap().get(key));
            }
        }
        return env;
    }

    public void updateParserProgress(ProgressState state, Integer progress, Integer count) {
        if (!this.isOneClick) {
            return;
        }
        OneClickProgress oneClickProgress = new OneClickProgress();
        oneClickProgress.setAppId(this.param.getApp().getAppId());
        oneClickProgress.setLogType(this.param.getLogType());
        ProgressInfo executorProgress = new ProgressInfo();
        executorProgress.setCount(count);
        executorProgress.setProgress(progress);
        executorProgress.setState(state);
        oneClickProgress.setProgressInfo(executorProgress);
        update(oneClickProgress);
    }

}
