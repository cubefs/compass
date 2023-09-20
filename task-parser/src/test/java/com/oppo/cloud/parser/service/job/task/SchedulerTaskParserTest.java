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

package com.oppo.cloud.parser.service.job.task;

import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.job.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@Slf4j
@SpringBootTest
class SchedulerTaskParserTest {

    LogRecord logRecord() {
        LogRecord logRecord = new LogRecord();
        List<App> apps = new ArrayList<>();
        App app = new App();
        app.setAppId("appid");
        app.setTryNumber(0);

        Map<String, List<LogPath>> logPathMap = new HashMap<>();

        LogPath scheduler = new LogPath();
        scheduler.setProtocol("hdfs");
        scheduler.setLogType("scheduler");
        scheduler.setLogPath("hdfs://localhost/appid/schedul*");
        List<LogPath> list = new ArrayList<>();
        list.add(scheduler);
        logPathMap.put("scheduler", list);

        apps.add(app);

        logRecord.setApps(apps);
        return logRecord;
    }

}
