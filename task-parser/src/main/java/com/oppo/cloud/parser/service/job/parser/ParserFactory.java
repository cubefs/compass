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
import com.oppo.cloud.common.util.spring.SpringBeanUtil;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.config.CustomConfig;
import com.oppo.cloud.parser.config.DiagnosisConfig;
import com.oppo.cloud.parser.config.ThreadPoolConfig;
import com.oppo.cloud.parser.service.rules.JobRulesConfigService;
import com.oppo.cloud.parser.service.writer.OpenSearchWriter;
import com.oppo.cloud.parser.service.writer.ParserResultSink;

import java.util.List;
import java.util.concurrent.Executor;


public class ParserFactory implements IParserFactory {

    @Override
    public DetectorConfig getDetectorConf() {
        return ((JobRulesConfigService) SpringBeanUtil.getBean(
                JobRulesConfigService.class)).detectorConfig;
    }

    @Override
    public List<ParserAction> getParserActions(LogType logType) {
        return DiagnosisConfig.getInstance().getActions(logType.getName());
    }

    @Override
    public Executor getTaskExecutor() {
        return (Executor) SpringBeanUtil.getBean(ThreadPoolConfig.PARSER_THREAD_POOL);
    }

    @Override
    public List<String> getJvmList() {
        return (List<String>) SpringBeanUtil.getBean(CustomConfig.GC_CONFIG);
    }

    @Override
    public ParserResultSink getParserResultSink() {
        // TODO implement more kinds of writers and let them configurable.
        ParserResultSink parserResultSink = new ParserResultSink();
        parserResultSink.register(OpenSearchWriter.getInstance());
        return parserResultSink;
    }

}
