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

package com.oppo.cloud.parser.service.writer;

import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.domain.job.ParserParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * a writer to log parser results, now just for testing
 */
@Slf4j
public class LogWriter implements IParserResultWriter {

    private List<ParserAction> parserActionList;

    public LogWriter() {
        this.parserActionList = new ArrayList<>();
    }

    @Override
    public void write(String logType, String logPath, ParserParam param, ParserAction parserAction) {
        log.info("Parsed results for logType {}," +
                "logPath {}, param {}, parserAction {}",
                logType, logPath, param, parserAction);
        parserActionList.add(parserAction);
    }

    public List<ParserAction> getParserActionList() {
        return parserActionList;
    }
}
