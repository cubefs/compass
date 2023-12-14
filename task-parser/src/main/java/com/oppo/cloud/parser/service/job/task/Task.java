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

import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.TaskParam;
import com.oppo.cloud.parser.domain.job.TaskResult;
import com.oppo.cloud.parser.service.job.oneclick.ProgressListener;
import com.oppo.cloud.parser.service.job.parser.IParser;
import com.oppo.cloud.parser.service.job.parser.ParserFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public abstract class Task {

    private final TaskParam taskParam;

    public Task(TaskParam taskParam) {
        this.taskParam = taskParam;
    }

    public List<IParser> createTasks() {
        List<IParser> parsers = new ArrayList<>();
        Map<String, List<LogPath>> logPathMap = this.taskParam.getLogInfo().getLogPathMap();
        if (logPathMap != null && logPathMap.size() > 0) {
            for (Map.Entry<String, List<LogPath>> map : logPathMap.entrySet()) {
                ParserFactory parserFactory = new ParserFactory();
                IParser parser = parserFactory.create(new ParserParam(map.getKey(), this.taskParam.getLogRecord(),
                        this.taskParam.getApp(), map.getValue()), new ProgressListener());
                if (parser != null) {
                    parsers.add(parser);
                }
            }
        }
        return parsers;
    }

    public List<CompletableFuture<CommonResult>> createFutures(List<IParser> parsers, Executor taskThreadPool) {

        List<CompletableFuture<CommonResult>> futures = new ArrayList<>();

        for (IParser parser : parsers) {
            CompletableFuture<CommonResult> future = CompletableFuture.supplyAsync(() -> {
                CommonResult commonResult = null;
                try {
                    commonResult = parser.run();
                } catch (Exception e) {
                    log.error("Exception:", e);
                }
                return commonResult;
            }, taskThreadPool);
            futures.add(future);
        }

        return futures;
    }

    public abstract TaskResult run();
}
