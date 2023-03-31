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

import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.parser.service.reader.HDFSReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class HDFSReaderTest {

    @Test
    void readLines() throws Exception {
        LogPath logPath = new LogPath();
        logPath.setLogPath("hdfs://logs-hdfs:8020/logs/application_1673850090992_26132");
        logPath.setProtocol("hdfs");
        HDFSReader hdfsReader = new HDFSReader(logPath);
        while (true){
            String line  = hdfsReader.getReaderObject().getBufferedReader().readLine();
            if(line == null ){
                break;
            }
            System.out.println(line);
        }
    }

    @Test
    void files() throws Exception {
        LogPath logPath = new LogPath();
        logPath.setLogPath("hdfs://logs-hdfs:8020/tmp/logs/root/logs/application_1673850090992_23171");
        logPath.setProtocol("hdfs");
        HDFSReader hdfsReader = new HDFSReader(logPath);
        List<String> list = null;
        try {
            list = hdfsReader.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(list);
    }
}
