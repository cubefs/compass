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
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.service.job.oneclick.ParserListenerBus;
import com.oppo.cloud.parser.service.reader.ILogReaderFactory;
import com.oppo.cloud.parser.service.reader.IReader;

public abstract class IParser extends ParserListenerBus {

    protected ParserParam param;

    protected ILogReaderFactory logReaderFactory;

    public IParser(ParserParam param, ILogReaderFactory logReaderFactory) {
        this.param = param;
        this.logReaderFactory = logReaderFactory;
    }

    public CommonResult run() {
        return null;
    }

    public IReader getReader(LogPath logPath) throws Exception {
        return logReaderFactory.create(logPath);
    }

    public void updateParserProgress(ProgressState state, Integer progress, Integer count) {
        if (!this.param.getLogRecord().getIsOneClick()) {
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
