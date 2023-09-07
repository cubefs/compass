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

package com.oppo.cloud.portal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.opensearch.LogSummary;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.mapper.TaskDiagnosisAdviceMapper;
import com.oppo.cloud.model.TaskDiagnosisAdvice;
import com.oppo.cloud.model.TaskDiagnosisAdviceExample;
import com.oppo.cloud.portal.domain.log.LogInfo;
import com.oppo.cloud.portal.domain.task.JobDetailRequest;
import com.oppo.cloud.portal.service.OpenSearchService;
import com.oppo.cloud.portal.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LogServiceImpl implements LogService {

    @Value(value = "${custom.opensearch.logIndex.name}")
    private String logSumIndex;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpenSearchService openSearchService;

    @Autowired
    private TaskDiagnosisAdviceMapper diagnoseAdviceMapper;

    @Override
    public List<LogInfo> getDiagnosticDetect(List<TaskApp> taskApps) {
        List<LogInfo> res = new ArrayList<>();
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andLogTypeEqualTo("yarn");
        List<TaskDiagnosisAdvice> diagnoseAdviceList =
                diagnoseAdviceMapper.selectByExampleWithBLOBs(diagnoseAdviceExample);
        for (TaskApp taskApp : taskApps) {
            if (StringUtils.isNotBlank(taskApp.getDiagnostics())) {
                res.add(LogInfo.genLogInfo(taskApp, diagnoseAdviceList));
            }
        }
        return res;
    }

    @Override
    public List<LogInfo> getLogDetect(JobDetailRequest jobDetailRequest, String logType) {
        List<LogInfo> res = new ArrayList<>();
        Map<String, Object> termQuery = jobDetailRequest.getTermQuery();
        Map<String, SortOrder> sort = new HashMap<>();
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andLogTypeEqualTo(logType);
        List<TaskDiagnosisAdvice> diagnoseAdviceList = diagnoseAdviceMapper.selectByExample(diagnoseAdviceExample);
        Map<String, TaskDiagnosisAdvice> actionMatch = new HashMap<>();
        List<String> actions = new ArrayList<>();
        for (TaskDiagnosisAdvice diagnoseAdvice : diagnoseAdviceList) {
            actionMatch.put(diagnoseAdvice.getAction(), diagnoseAdvice);
            actions.add(diagnoseAdvice.getAction());
        }
        if (jobDetailRequest.getTryNumber() != null && jobDetailRequest.getTryNumber() != 0) {
            termQuery.put("retryTimes", jobDetailRequest.getTryNumber());
        }
        termQuery.put("action.keyword", actions);
        // todo:add: sort.put("logTimestamp", SortOrder.ASC);
        sort.put("logTimestamp", SortOrder.ASC);
        SearchSourceBuilder searchSourceBuilder = openSearchService.genSearchBuilder(termQuery, null, sort, null);
        searchSourceBuilder.size(1000);
        List<LogSummary> logSumList;
        try {
            logSumList = openSearchService.find(LogSummary.class, searchSourceBuilder, logSumIndex + "-*");
        } catch (Exception e) {
            log.error("openSearchService.find failed:{}", e.getMessage());
            return null;
        }
        Set<Integer> logSet = new HashSet<>();
        for (LogSummary logSum : logSumList) {
            int logKey = (logSum.getAction() + logSum.getRawLog()).hashCode();
            if (logSet.contains(logKey)) {
                continue;
            } else {
                logSet.add(logKey);
            }
            TaskDiagnosisAdvice diagnoseAdvice = actionMatch.get(logSum.getAction());
            LogInfo logInfo;
            try {
                logInfo = LogInfo.genLogInfo(logSum, diagnoseAdvice);
            } catch (Exception e) {
                log.error("genLogInfo from logSum failed, msg:{}", e.getMessage());
                continue;
            }
            res.add(logInfo);
        }
        return res;
    }
}
