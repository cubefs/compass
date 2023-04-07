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

import com.oppo.cloud.common.domain.elasticsearch.LogSummary;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.mapper.TaskDiagnosisAdviceMapper;
import com.oppo.cloud.model.TaskDiagnosisAdvice;
import com.oppo.cloud.model.TaskDiagnosisAdviceExample;
import com.oppo.cloud.portal.domain.base.Conclusion;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.domain.diagnose.Table;
import com.oppo.cloud.portal.domain.diagnose.runerror.RunError;
import com.oppo.cloud.portal.domain.log.LogInfo;
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.service.diagnose.Generate;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * 运行错误基类
 */
@Slf4j
public abstract class RunErrorBaseService implements Generate {

    @Autowired
    TaskDiagnosisAdviceMapper diagnosisAdviceMapper;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Value(value = "${custom.elasticsearch.logIndex.name}")
    String logIndex;

    /**
     * 获取app异常类型(需要子类实现)
     */
    public abstract String getCategory();

    /**
     * 报告描述（需要各个子类实现）
     */
    public abstract String generateItemDesc();

    /**
     * 生产分析结论
     */
    public abstract Conclusion generateConclusion();

    /**
     * 产生该模块的诊断建议集合,需要各个子类实现
     */
    public List<TaskDiagnosisAdvice> getDiagnosisAdvice() {
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andCategoryEqualTo(this.getCategory());
        return diagnosisAdviceMapper.selectByExampleWithBLOBs(diagnoseAdviceExample);
    }

    /**
     * 产生该类报告的模板方法
     */
    @Override
    public Item<RunError> generate(DetectorStorage detectorStorage) {
        List<LogInfo> res;
        try {
            res = this.generateData(detectorStorage.getApplicationId());
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            return this.generateItem(null, baos.toString());
        }
        // 统一规范，没有数据的都为null
        res = res.size() == 0 ? null : res;
        // 生成诊断报告
        return this.generateItem(res, null);
    }

    public Item<RunError> generate(String applicationId) {
        List<LogInfo> res;
        try {
            res = this.generateData(applicationId);
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            return this.generateItem(null, baos.toString());
        }
        // 统一规范，没有数据的都为null
        res = res.size() == 0 ? null : res;
        // 生成诊断报告
        return this.generateItem(res, null);
    }

    public List<LogInfo> generateData(String applicationId) throws Exception {
        List<LogInfo> res = new ArrayList<>();

        List<TaskDiagnosisAdvice> diagnoseAdviceList = this.getDiagnosisAdvice();
        List<String> actionList = new ArrayList<>();
        Map<String, TaskDiagnosisAdvice> actionMap = new HashMap<>();
        for (TaskDiagnosisAdvice diagnoseAdvice : diagnoseAdviceList) {
            actionList.add(diagnoseAdvice.getAction());
            actionMap.put(diagnoseAdvice.getAction(), diagnoseAdvice);
        }
        HashMap<String, Object> termQueryConditions = new HashMap<>();
        HashMap<String, SortOrder> sortConditions = new HashMap<>();
        termQueryConditions.put("applicationId.keyword", applicationId);
        termQueryConditions.put("action.keyword", actionList);
        // todo
        // sortConditions.put("timestamp", SortOrder.ASC);
        // 查询Es信息
        SearchSourceBuilder searchSourceBuilder =
                elasticSearchService.genSearchBuilder(termQueryConditions, null, sortConditions, null);
        List<LogSummary> logSumList = elasticSearchService.find(LogSummary.class, searchSourceBuilder, logIndex + "-*");
        // 根据es产生诊断数据
        Set<Integer> logSet = new HashSet<>();
        for (LogSummary logSum : logSumList) {
            int logKey = (logSum.getAction() + logSum.getRawLog()).hashCode();
            if (logSet.contains(logKey)) {
                continue;
            } else {
                logSet.add(logKey);
            }
            TaskDiagnosisAdvice diagnoseAdvice = actionMap.get(logSum.getAction());
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

    public Item<RunError> generateItem(List<LogInfo> data, String error) {
        Item<RunError> item = new Item<>();
        item.setName(this.generateItemDesc());
        item.setItem(this.generateTable(data));
        Conclusion conclusion = this.generateConclusion();
        if (data == null || data.size() == 0) {
            conclusion.setConclusion("未检测到异常");
        }
        item.setConclusion(conclusion);
        item.setError(error);
        item.setType("table");
        return item;
    }

    public RunError generateTable(List<LogInfo> data) {
        RunError runError = new RunError();
        if (data != null && data.size() != 0) {
            runError.setAbnormal(true);
            Table<LogInfo> logInfoTable = runError.getTable();
            logInfoTable.setData(data);
            return runError;
        }
        return null;
    }
}
