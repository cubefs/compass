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

package com.oppo.cloud.parser.service.job;

import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.parser.service.ParamUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class JobManagerTest {

    @Resource
    JobManager jobManager;

    @Test
    void run() {
        // String jsonStr = "{}";
        // LogRecord logRecord = JSON.parseObject(jsonStr, LogRecord.class);
        LogRecord logRecord = ParamUtil.getLogRecord();
        try {
            jobManager.run(logRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
