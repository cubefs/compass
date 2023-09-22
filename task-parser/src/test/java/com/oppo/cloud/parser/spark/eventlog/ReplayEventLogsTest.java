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

import com.oppo.cloud.parser.utils.ReplaySparkEventLogs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;

@Slf4j
class ReplayEventLogsTest {

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
}
