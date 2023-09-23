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

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.common.util.textparser.ParserManager;
import com.oppo.cloud.parser.config.DiagnosisConfig;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.TaskParam;
import com.oppo.cloud.parser.domain.job.TaskResult;
import com.oppo.cloud.parser.service.writer.OpenSearchWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Yarn diagnostics log
 */
@Slf4j
public class YarnTask extends Task {

    private final TaskParam taskParam;

    public YarnTask(TaskParam taskParam) {
        super(taskParam);
        this.taskParam = taskParam;
    }

    @Override
    public TaskResult run() {
        List<ParserAction> actions = DiagnosisConfig.getInstance().getActions(LogType.YARN.getName());
        String appId = this.taskParam.getApp().getAppId();
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        TaskApp taskApp = this.taskParam.getLogRecord().getTaskAppList().get(appId);
        if (taskApp == null) {
            return null;
        }
        String diagnostics = taskApp.getDiagnostics();
        if (StringUtils.isBlank(diagnostics)) {
            return null;
        }

        Map<String, ParserAction> results = ParserManager.parse(new String[]{diagnostics}, actions);
        if (results == null || results.size() == 0) {
            List<String> list = new ArrayList<>();
            list.add(AppCategoryEnum.OTHER_EXCEPTION.getCategory());
            return new TaskResult(this.taskParam.getApp().getAppId(), list);
        }

        ParserParam param = new ParserParam(LogType.YARN.getName(), this.taskParam.getLogRecord(),
                this.taskParam.getApp(), null);

        List<String> list = OpenSearchWriter.getInstance()
                .saveParserActions(LogType.YARN.getName(), "", param, results);

        return new TaskResult(this.taskParam.getApp().getAppId(), new ArrayList<>(list));
    }
}
