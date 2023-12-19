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
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.util.spring.SpringBeanUtil;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.parser.config.CustomConfig;
import com.oppo.cloud.parser.config.DiagnosisConfig;
import com.oppo.cloud.parser.config.ThreadPoolConfig;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.SparkExecutorLogParserResult;
import com.oppo.cloud.parser.service.ParamUtil;
import com.oppo.cloud.parser.utils.ResourcePreparer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;

@SpringBootTest
class SparkExecutorLogParserTest extends ResourcePreparer {

    @Test
    void run() {
        LogRecord logRecord = ParamUtil.getLogRecord();
        Map<String, List<LogPath>> logPathMap = logRecord.getApps().get(0).getLogInfoList().get(1).getLogPathMap();

        ParserParam param = new ParserParam(
                LogType.SPARK_DRIVER.getName(),
                logRecord, logRecord.getApps().get(0),
                logPathMap.get(LogType.SPARK_DRIVER.getName())
        );

        ThreadPoolTaskExecutor parserThreadPool = (ThreadPoolTaskExecutor)
                SpringBeanUtil.getBean(ThreadPoolConfig.PARSER_THREAD_POOL);
        List<String> jvmTypeList = (List<String>) SpringBeanUtil.getBean(CustomConfig.GC_CONFIG);
        List<ParserAction> actions = DiagnosisConfig.getInstance().getActions(LogType.SPARK_DRIVER.getName());

        SimpleParserFactory simpleParserFactory = new SimpleParserFactory();
        SparkExecutorLogParser parser = new SparkExecutorLogParser(param,
                simpleParserFactory.createLogReaderFactory(),
                actions,
                ParserTestUtil.getLogSink(),
                parserThreadPool,
                jvmTypeList);
        CommonResult commonResult = parser.run();
        List<SparkExecutorLogParserResult> results = (List<SparkExecutorLogParserResult>) commonResult.getResult();
        Assertions.assertTrue(results.size() == 1);
        SparkExecutorLogParserResult result = results.get(0);
        Assertions.assertTrue(result.getActionMap().size() == 1);
        Assertions.assertTrue(result.getActionMap().containsKey("jobFailedOrAbortedException"));
    }
}
