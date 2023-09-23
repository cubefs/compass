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

package com.oppo.cloud.parser.service.job.reader;

import com.oppo.cloud.common.constant.ProtocolType;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.parser.service.reader.HDFSReader;
import com.oppo.cloud.parser.utils.MiniHdfsCluster;
import com.oppo.cloud.parser.utils.ResourcePreparer;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;


@SpringBootTest
@Slf4j
class HDFSReaderTest extends ResourcePreparer {

    private static String PROTOCAL_TYPE = ProtocolType.HDFS.getName();

    @Test
    void listFiles() throws Exception {
        LogPath logPath = new LogPath();
        logPath.setLogPath(getTextLogDir());
        logPath.setProtocol(PROTOCAL_TYPE);
        HDFSReader hdfsReader = new HDFSReader(logPath);
        List<String> list = null;
        try {
            list = hdfsReader.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertTrue(list.size() > 0);
        }
    }

    @Test
    void readLines() throws Exception {
        LogPath logDirPath = new LogPath();
        logDirPath.setLogPath(getTextLogDir());
        logDirPath.setProtocol(PROTOCAL_TYPE);
        HDFSReader hdfsReader = new HDFSReader(logDirPath);
        final List<String> logFileList = hdfsReader.listFiles();
        Assertions.assertTrue(logFileList.size() > 0, "logs dir is empty.");
        for (String logFilePath : logFileList) {
            LogPath logPath = new LogPath();
            logPath.setLogPath(logFilePath);
            logPath.setProtocol(PROTOCAL_TYPE);
            HDFSReader logFileReader = new HDFSReader(logPath);
            final BufferedReader bufferedReader = logFileReader.getReaderObject().getBufferedReader();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                Assertions.assertTrue(!line.isEmpty());
            }
        }
    }
}