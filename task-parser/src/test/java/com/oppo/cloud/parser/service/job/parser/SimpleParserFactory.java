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
import com.oppo.cloud.common.domain.cluster.hadoop.NameNodeConf;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.config.DiagnosisConfig;
import com.oppo.cloud.parser.service.reader.ILogReaderFactory;
import com.oppo.cloud.parser.service.writer.LogWriter;
import com.oppo.cloud.parser.service.writer.ParserResultSink;
import com.oppo.cloud.parser.utils.ParserConfigLoader;
import com.oppo.cloud.parser.utils.ResourcePreparer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleParserFactory extends ResourcePreparer implements IParserFactory {

    public SimpleParserFactory() {
        DiagnosisConfig.getInstance().setRuleMap(getRulesConfig());
        DiagnosisConfig.getInstance().setDetectorConfig(getDetectorConfig());
    }

    @Override
    public DetectorConfig getDetectorConf() {
        return DiagnosisConfig.getInstance().detectorConfig;
    }

    @Override
    public List<ParserAction> getParserActions(LogType logType) {
        return DiagnosisConfig.getInstance().getActions(logType.getName());
    }

    @Override
    public Executor getTaskExecutor() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        return executorService;
    }

    @Override
    public List<String> getJvmList() {
        return Arrays.asList("Java HotSpot", "OpenJDK");
    }

    @Override
    public ParserResultSink getParserResultSink() {
        ParserResultSink parserResultSink = new ParserResultSink();
        parserResultSink.register(new LogWriter());
        return parserResultSink;
    }

    @Override
    public ILogReaderFactory createLogReaderFactory() {
        return new ILogReaderFactory() {
            @Override
            public Map<String, NameNodeConf> getNameNodeConf() {
                return ParserConfigLoader.getNameNodeConfMap();
            }
        };
    }
}
