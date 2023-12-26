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
import com.oppo.cloud.common.util.textparser.ParserActionUtil;
import com.oppo.cloud.parser.domain.job.ParserParam;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A collection of writers, responsible for calling multiple writers
 * to write parser results to different locations.
 */
@Slf4j
public class ParserResultSink {

    List<IParserResultWriter> writers;
    public ParserResultSink() {
        this.writers = new ArrayList<>();
    }

    public void register(IParserResultWriter writer) {
        this.writers.add(writer);
    }

    /**
     * Save matching results
     */
    public List<String> saveParserActions(String logType, String logPath, ParserParam param, Map<String, ParserAction> results) {
        List<String> categories = new ArrayList<>();
        results.forEach((k, v) -> {
            List<ParserAction> list = ParserActionUtil.getLeafAction(v, true);
            if (list.size() == 0) {
                log.error("getLeafAction:{},{}", k, v);
                list.add(v);
            }
            for (ParserAction parserAction : list) {
                categories.add(parserAction.getCategory());
                // write parsed data by all writers registered.
                for (IParserResultWriter writer : writers) {
                    writer.write(logType, logPath, param, parserAction);
                }
            }
        });
        return categories;
    }

}
