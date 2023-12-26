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

import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.service.job.oneclick.IProgressListener;
import com.oppo.cloud.parser.service.reader.ILogReaderFactory;
import com.oppo.cloud.parser.service.writer.ParserResultSink;

import java.util.List;
import java.util.concurrent.Executor;

public interface IParserFactory {

    DetectorConfig getDetectorConf();

    List<ParserAction> getParserActions(LogType logType);

    Executor getTaskExecutor();

    List<String> getJvmList();

    ParserResultSink getParserResultSink();

    ILogReaderFactory createLogReaderFactory();

    /**
     * create parser
     */
    default IParser create(ParserParam parserParam, IProgressListener listener) {
        IParser parser = createParserInternal(parserParam);
        if (parser != null && listener != null) {
            parser.addListener(listener);
        }
        return parser;
    }

    default IParser createParserInternal(ParserParam parserParam) {
        LogType logType = parserParam.getLogType();
        switch (logType) {

            case SCHEDULER:
                return new SchedulerLogParser(parserParam,
                        createLogReaderFactory(), getParserActions(logType), getParserResultSink());

            case SPARK_EVENT:
                return new SparkEventLogParser(parserParam,
                        createLogReaderFactory(), getDetectorConf());

            case SPARK_EXECUTOR:
                return new SparkExecutorLogParser(parserParam,
                        createLogReaderFactory(), getParserActions(logType), getParserResultSink(),
                        getTaskExecutor(), getJvmList());

            case MAPREDUCE_JOB_HISTORY:
                return new MapReduceJobHistoryParser(parserParam,
                        createLogReaderFactory(), getDetectorConf());

            case MAPREDUCE_CONTAINER:
                return new MapReduceContainerLogParser(parserParam,
                        createLogReaderFactory(), getParserActions(logType), getParserResultSink());

            default:
                return null;
        }
    }

}
