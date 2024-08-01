package com.oppo.cloud.parser.service.job.reader;

import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.SparkEventLogParserResult;
import com.oppo.cloud.parser.service.ParamUtil;
import com.oppo.cloud.parser.service.job.parser.IParser;
import com.oppo.cloud.parser.service.job.parser.SimpleParserFactory;
import com.oppo.cloud.parser.service.reader.LogReaderFactory;
import com.oppo.cloud.parser.utils.ResourcePreparer;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;

public class InputStreamReaderTest extends ResourcePreparer {
    @Test
    public void inputStreamReaderTest() {
        String eventLogPath = "log/event/eventlog";
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(eventLogPath);
        SimpleParserFactory simpleParserFactory = new SimpleParserFactory();
        LogRecord logRecord = ParamUtil.getLogRecord();
        String logType = LogType.SPARK_EVENT.getName();
        LogPath logPath = new LogPath();
        logPath.setInputStream(inputStream);
        logPath.setProtocol(LogReaderFactory.STREAM);
        logPath.setLogType(logType);
        logPath.setLogPath(eventLogPath);
        ParserParam parserParam = new ParserParam(logType,
                logRecord,
                logRecord.getApps().get(0),
                Collections.singletonList(logPath));
        IParser parser = simpleParserFactory.createParserInternal(parserParam);
        CommonResult commonResult = parser.run();
        SparkEventLogParserResult result = (SparkEventLogParserResult) commonResult.getResult();
        Assert.assertTrue(result.getMemoryCalculateParam().getDriverMemory() == 536870912L);
        Assert.assertTrue(result.getMemoryCalculateParam().getAppTotalTime() == 588898L);

    }
}
