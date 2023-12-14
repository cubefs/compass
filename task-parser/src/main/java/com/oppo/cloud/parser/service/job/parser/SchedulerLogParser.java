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

import com.oppo.cloud.common.constant.ProgressState;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.oneclick.OneClickProgress;
import com.oppo.cloud.common.domain.oneclick.ProgressInfo;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.service.reader.IReader;
import com.oppo.cloud.parser.service.reader.LogReaderFactory;
import com.oppo.cloud.parser.service.writer.OpenSearchWriter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SchedulerLogParser extends CommonTextParser {

    private final ParserParam param;

    private boolean isOneClick;

    private List<ParserAction> actions;

    public SchedulerLogParser(ParserParam param,
                              List<ParserAction> actions) {
        this.param = param;
        this.actions = actions;
        this.isOneClick = param.getLogRecord().getIsOneClick();
    }

    @Override
    public CommonResult run() {
        CommonResult commonResult = new CommonResult();
        commonResult.setLogType(this.param.getLogType());
        List<String> categories = new ArrayList<>();
        updateParserProgress(ProgressState.PROCESSING, 0, 0);
        String logType = this.param.getLogType().getName();

        for (LogPath logPath : this.param.getLogPaths()) {
            List<ReaderObject> readerObjects;
            try {
                IReader reader = LogReaderFactory.create(logPath);
                readerObjects = reader.getReaderObjects();
            } catch (Exception e) {
                log.error("Exception:", e);
                continue;
            }
            updateParserProgress(ProgressState.PROCESSING, 0, readerObjects.size());
            for (ReaderObject readerObject : readerObjects) {
                Map<String, ParserAction> results;
                try {
                    results = super.parse(readerObject, actions);
                } catch (Exception e) {
                    log.error("Exception:", e);
                    continue;
                } finally {
                    readerObject.close();
                }
                List<String> list = OpenSearchWriter.getInstance()
                        .saveParserActions(logType, readerObject.getLogPath(), this.param, results);
                categories.addAll(list);
            }
        }

        commonResult.setResult(categories);
        updateParserProgress(ProgressState.SUCCEED, 0, 0);
        return commonResult;
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
        super.update(oneClickProgress);
    }
}
