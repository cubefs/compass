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

package com.oppo.cloud.portal.service.diagnose.runerror;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.model.TaskDiagnosisAdvice;
import com.oppo.cloud.model.TaskDiagnosisAdviceExample;
import com.oppo.cloud.portal.domain.base.Conclusion;
import com.oppo.cloud.portal.domain.log.LogInfo;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class OtherExceptionService extends RunErrorBaseService {

    @Value(value = "${custom.opensearch.appIndex.name}")
    String appIndex;

    @Override
    public String getCategory() {
        return AppCategoryEnum.OTHER_EXCEPTION.getCategory();

    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("OTHER_EXCEPTION_ANALYSIS");
    }

    @Override
    public Conclusion generateConclusion() {
        return new Conclusion(MessageSourceUtil.get("OTHER_EXCEPTION_CONCLUSION"), MessageSourceUtil.get("OTHER_EXCEPTION_CONCLUSION_DESC"));
    }

    @Override
    public List<LogInfo> generateData(String applicationId) throws Exception {
        // driver/executor log
        List<LogInfo> executorLog = super.generateData(applicationId);
        // yarn log
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", applicationId);
        List<TaskApp> taskAppList = openSearchService.find(TaskApp.class, termQuery, appIndex + "-*");
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andCategoryEqualTo("yarn");
        List<TaskDiagnosisAdvice> diagnoseAdviceList = diagnosisAdviceMapper.selectByExample(diagnoseAdviceExample);
        if (taskAppList.size() != 0) {
            TaskApp taskApp = taskAppList.get(0);
            for (TaskApp taskAppTemp : taskAppList) {
                if (taskAppTemp.getCategories() != null
                        && taskAppTemp.getCategories().size() > taskApp.getCategories().size()) {
                    taskApp = taskAppTemp;
                }
            }
            if (StringUtils.isNotEmpty(taskApp.getDiagnostics())) {
                LogInfo logInfo = LogInfo.genLogInfo(taskApp, diagnoseAdviceList);
                executorLog.add(logInfo);
            }
        }
        return executorLog;
    }
}
