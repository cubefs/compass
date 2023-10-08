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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.common.domain.job.App;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.detect.service.LogRecordService;
import com.oppo.cloud.detect.service.SchedulerLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *  Log parsing message service.
 */
@Service
@Slf4j
public class LogRecordServiceImpl implements LogRecordService {

    @Autowired
    SchedulerLogService schedulerLogService;


    @Override
    public List<App> getSchedulerLog(JobAnalysis detectJobAnalysis) {
        List<App> apps = new ArrayList<>();
        // Construct scheduling logs based on the number of times the task is retried.
        for (int i = 0; i <= detectJobAnalysis.getRetryTimes(); i++) {
            List<String> logPaths = schedulerLogService.getSchedulerLog(detectJobAnalysis.getProjectName(),
                    detectJobAnalysis.getFlowName(), detectJobAnalysis.getTaskName(),
                    detectJobAnalysis.getExecutionDate(), i);
            if (logPaths != null && logPaths.size() != 0) {
                App app = new App();
                app.formatSchedulerLog(logPaths, i);
                apps.add(app);
            }
        }
        // Successfully constructing the scheduling log for each retry.
        if (apps.size() < detectJobAnalysis.getRetryTimes() + 1) {
            apps = new ArrayList<>();
        }
        return apps;
    }

    @Override
    public List<App> getAppLog(List<TaskApp> taskAppList) {
        List<App> apps = new ArrayList<>();
        // If there is a complete appId, send it together.
        for (TaskApp taskApp : taskAppList) {
            App app = new App();
            app.formatAppLog(taskApp);
            apps.add(app);
        }
        return apps;
    }
}
