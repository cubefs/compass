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

import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.service.reader.ILogReaderFactory;
import com.oppo.cloud.parser.service.writer.ParserResultSink;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MapReduceContainerLogParser extends CommonTextParser {

    public MapReduceContainerLogParser(ParserParam param,
                                       ILogReaderFactory logReaderFactory,
                                       List<ParserAction> actions,
                                       ParserResultSink parserResultSink) {
        super(param, logReaderFactory, actions, parserResultSink);
    }

}
