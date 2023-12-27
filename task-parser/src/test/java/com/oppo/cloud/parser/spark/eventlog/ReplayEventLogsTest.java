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

package com.oppo.cloud.parser.spark.eventlog;

import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.SparkEventLogParserResult;
import com.oppo.cloud.parser.domain.job.SparkExecutorLogParserResult;
import com.oppo.cloud.parser.service.ParamUtil;
import com.oppo.cloud.parser.service.job.parser.SimpleParserFactory;
import com.oppo.cloud.parser.service.job.parser.SparkEventLogParser;
import com.oppo.cloud.parser.service.reader.ILogReaderFactory;
import com.oppo.cloud.parser.utils.ReplaySparkEventLogs;
import com.oppo.cloud.parser.utils.ResourcePreparer;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
class ReplayEventLogsTest extends ResourcePreparer {

    @Test
    void replay() throws Exception {
        File file = new File(ReplayEventLogsTest.class.getClassLoader().getResource("log/event/eventlog").getPath());
        String content = new String(Files.readAllBytes(file.toPath()));
        String[] lines = content.split("\n");
        ReplaySparkEventLogs replayEventLogs = new ReplaySparkEventLogs();
        replayEventLogs.replay(lines);
        Assertions.assertTrue(replayEventLogs.getApplication().getAppEndTimestamp() == 1657505955279L);
        Assertions.assertTrue(replayEventLogs.getJobs().size() == 1);
        Assertions.assertTrue(replayEventLogs.getExecutors().size() == 1);
        Assertions.assertTrue(replayEventLogs.getTasks().size() == 4);
    }

    @Test
    void lz4CompressedReplay() throws IOException {
        final String originalEventLogName = "eventlog";
        final String lz4EventLogName = "eventlog.lz4";
        final String eventLogPath = ReplayEventLogsTest.class.getClassLoader().
                getResource("log/event/" + originalEventLogName).getPath();
        String eventLogDir = eventLogPath.split("eventlog")[0];
        String lz4CompressedEventLogPath = new File(eventLogDir + lz4EventLogName).getPath();
        try {
            final File eventLogFile = new File(eventLogPath);
            final String readFromOriginal = new String(Files.readAllBytes(eventLogFile.toPath()));
            final FileOutputStream fileOutputStream = new FileOutputStream(lz4CompressedEventLogPath);
            final LZ4BlockOutputStream lz4BlockOutputStream = new LZ4BlockOutputStream(fileOutputStream);
            final byte[] contentBytes = readFromOriginal.getBytes(Charset.forName("UTF-8"));
            lz4BlockOutputStream.write(contentBytes);
            lz4BlockOutputStream.flush();
            lz4BlockOutputStream.close();
            // de-compress
            final Path path = Paths.get(lz4CompressedEventLogPath);
            final LZ4BlockInputStream lz4BlockInputStream = new LZ4BlockInputStream(Files.newInputStream(path));
            final BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(lz4BlockInputStream));
            String readFromCompressed = bufferedReader.lines().collect(Collectors.joining( "\n"));
            readFromCompressed += "\n";
            // check
            Assertions.assertTrue(readFromCompressed.equals(readFromOriginal));

            //upload local compressed event log to hdfs
            ResourcePreparer.prepareResources();

            //create param
            LogRecord logRecord = ParamUtil.getLogRecord();
            Map<String, List<LogPath>> logPathMap = logRecord.getApps().get(0).getLogInfoList().get(1).getLogPathMap();
            List<LogPath> logPaths = logPathMap.get(LogType.SPARK_EVENT.getName());
            logPaths.get(0).setLogPath(getTextLogDir() + "/event/" + lz4EventLogName);
            ParserParam param = new ParserParam(
                    LogType.SPARK_EVENT.getName(),
                    logRecord, logRecord.getApps().get(0),
                    logPaths
            );
            SimpleParserFactory simpleParserFactory = new SimpleParserFactory();
            ILogReaderFactory logReaderFactory = simpleParserFactory.createLogReaderFactory();
            DetectorConfig detectorConf = simpleParserFactory.getDetectorConf();
            SparkEventLogParser sparkEventLogParser = new SparkEventLogParser(param, logReaderFactory, detectorConf);
            CommonResult commonResult = sparkEventLogParser.run();
            System.out.println(commonResult);
            SparkEventLogParserResult result = (SparkEventLogParserResult) commonResult.getResult();
            Assertions.assertTrue(result.getDetectorStorage().getLogPath().endsWith(".lz4"));
            Assertions.assertTrue(result.getDetectorStorage().getEnv().size() == 10);
            Assertions.assertTrue(result.getDetectorStorage().getDataList().size() == 4);
        } finally {
            getFileSystem().delete(new org.apache.hadoop.fs.Path(getTextLogDir() + "/event/" + lz4EventLogName));
            new File(lz4CompressedEventLogPath).delete();
        }
    }
}
