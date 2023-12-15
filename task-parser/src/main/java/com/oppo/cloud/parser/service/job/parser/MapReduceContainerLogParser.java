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
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.service.reader.IReader;
import com.oppo.cloud.parser.service.reader.LogReaderFactory;
import com.oppo.cloud.parser.service.writer.OpenSearchWriter;
import com.oppo.cloud.parser.service.writer.ParserResultSink;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MapReduceContainerLogParser extends CommonTextParser {

    public MapReduceContainerLogParser(ParserParam param,
                                       List<ParserAction> actions,
                                       ParserResultSink parserResultSink) {
        super(param, actions, parserResultSink);
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
                    results = super.parse(readerObject);
                } catch (Exception e) {
                    log.error("Exception:", e);
                    continue;
                } finally {
                    readerObject.close();
                }
                List<String> list = getSink().saveParserActions(
                        logType, readerObject.getLogPath(), this.param, results);
                categories.addAll(list);
            }
        }

        commonResult.setResult(categories);
        updateParserProgress(ProgressState.SUCCEED, 0, 0);
        return commonResult;
    }

}
