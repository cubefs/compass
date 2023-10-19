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

package com.oppo.cloud.parser.service.writer;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.opensearch.OpenSearchInfo;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.LogSummary;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.gc.ExecutorPeakMemory;
import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.domain.job.LogRecord;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.common.util.opensearch.BulkApi;
import com.oppo.cloud.common.util.opensearch.UpdateApi;
import com.oppo.cloud.common.util.spring.SpringBeanUtil;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.common.util.textparser.ParserActionUtil;
import com.oppo.cloud.common.util.textparser.ParserResult;
import com.oppo.cloud.parser.config.CustomConfig;
import com.oppo.cloud.parser.config.OpenSearchConfig;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.TaskResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.*;

/**
 * OpenSearch writer
 */
@Slf4j
public class OpenSearchWriter {

    public RestHighLevelClient client;

    public String logSummaryPrefix;

    public String detectorPrefix;

    public String gcPrefix;

    public String jobPrefix;

    public String taskAppPrefix;

    private OpenSearchWriter() {
        client = (RestHighLevelClient) SpringBeanUtil.getBean(OpenSearchConfig.SEARCH_CLIENT);
        CustomConfig yml = (CustomConfig) SpringBeanUtil.getBean(CustomConfig.class);
        logSummaryPrefix = yml.getLogSummaryPrefix();
        detectorPrefix = yml.getDetectorPrefix();
        gcPrefix = yml.getGcPrefix();
        jobPrefix = yml.getJobPrefix();
        taskAppPrefix = yml.getTaskAppPrefix();
    }

    public static OpenSearchWriter getInstance() {
        return OpenSearch.INSTANCE.getInstance();
    }

    /**
     * Save matching results
     */
    public List<String> saveParserActions(String logType, String logPath, ParserParam param, Map<String, ParserAction> results) {
        List<String> categories = new ArrayList<>();
        results.forEach((k, v) -> {
            List<ParserAction> list = ParserActionUtil.getLeafAction(v, true);
            if (list.size() == 0) {
                log.error("getLeafAction:{},{}", k, v);
                list.add(v);
            }
            for (ParserAction parserAction : list) {
                categories.add(parserAction.getCategory());
                writeToOpenSearch(logType, logPath, param, parserAction);
            }
        });
        return categories;
    }

    /**
     * Write matching results to OpenSearch
     */
    public void writeToOpenSearch(String logType, String logPath, ParserParam param, ParserAction parserAction) {
        if (parserAction.getParserResults() != null) {
            List<Map<String, Object>> docs = new ArrayList<>();
            for (ParserResult parserResult : parserAction.getParserResults()) {
                Map<String, Object> logSummary;
                try {
                    logSummary = getDoc(logType, parserResult, logPath, param, parserAction);
                } catch (Exception e) {
                    log.error("logSummaryGetDoc:{},{}", logPath, e);
                    continue;
                }
                docs.add(logSummary);
            }
            if (docs.size() > 0) {
                BulkResponse response;
                try {
                    String index = logSummaryPrefix + DateUtil.formatToDay(param.getLogRecord().getJobAnalysis().getExecutionDate());
                    response = BulkApi.bulk(client, index, docs);
                } catch (Exception e) {
                    log.error("writeLogSummaryErr:{},{}", logPath, e);
                    return;
                }
                BulkItemResponse[] responses = response.getItems();

                for (BulkItemResponse r : responses) {
                    if (r.isFailed()) {
                        log.info("writeLogSummaryErr:{},{}", logPath, r.getFailure().getCause());
                    }
                }
                log.info("writeLogSummaryCount:{},{},{},{}", logType, logPath, parserAction.getAction(), docs.size());
            }
        }
    }

    /**
     * Get document content
     */
    public Map<String, Object> getDoc(String logType, ParserResult parserResult, String logPath, ParserParam param,
                                      ParserAction parserAction) throws Exception {
        LogSummary logSummary = new LogSummary();
        logSummary.setApplicationId(param.getApp().getAppId());
        logSummary.setLogType(logType);
        logSummary.setProjectName(param.getLogRecord().getJobAnalysis().getProjectName());
        logSummary.setFlowName(param.getLogRecord().getJobAnalysis().getFlowName());
        logSummary.setTaskName(param.getLogRecord().getJobAnalysis().getTaskName());
        logSummary.setExecutionDate(param.getLogRecord().getJobAnalysis().getExecutionDate());
        logSummary.setRetryTimes(param.getApp().getTryNumber());
        logSummary.setAction(parserAction.getAction());
        logSummary.setAction(parserAction.getAction());
        logSummary.setStep(parserAction.getStep());
        logSummary.setGroupNames(Arrays.asList(parserAction.getGroupNames()));
        logSummary.setRawLog(String.join("\n", parserResult.getLines()));
        logSummary.setLogPath(logPath);
        logSummary.setGroupData(parserResult.getGroupData());

        if (parserResult.getGroupData() != null) {
            long timestamp;
            try {
                String dateStr = parserResult.getGroupData().get("datetime");
                // fix datetime
                if (dateStr.contains("/")) {
                    dateStr = "20" + dateStr;
                    dateStr = dateStr.replace("/", "-");
                }
                timestamp = DateUtil.dateToTimeStamp(dateStr);
            } catch (Exception e) {
                log.error("get datetime err:{},{}", param.getApp().getAppId(), parserResult.getGroupData());
                timestamp = System.currentTimeMillis() / 1000;
            }
            logSummary.setLogTimestamp((int) timestamp);
        } else {
            logSummary.setLogTimestamp((int) (System.currentTimeMillis() / 1000));

        }
        return logSummary.genDoc();
    }

    public void saveDetectorStorage(DetectorStorage detectionStorage) {
        String index = detectorPrefix + DateUtil.formatToDay(detectionStorage.getExecutionTime());
        UpdateApi api = new UpdateApi();
        try {
            UpdateResponse resp = api.upsertJson(client, index, detectionStorage.getApplicationId(), JSON.toJSONString(detectionStorage));
            log.info("saveDetectorStorage:{},{}", detectionStorage.getApplicationId(), resp);
        } catch (Exception e) {
            log.info("saveDetectorStorageErr:{},{}", detectionStorage.getApplicationId(), e);
        }
    }

    /**
     * Update job categories information
     */
    public void updateJob(JobAnalysis jobAnalysis, Map<String, Boolean> categoryMap) throws Exception {
        if (jobAnalysis == null || jobAnalysis.getTaskName() == null) {
            return;
        }
        UpdateApi api = new UpdateApi();
        String index = jobPrefix + DateUtil.formatToDay(jobAnalysis.getExecutionDate());
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery("projectName.keyword", jobAnalysis.getProjectName()));
        boolQueryBuilder.filter(QueryBuilders.termQuery("flowName.keyword", jobAnalysis.getFlowName()));
        boolQueryBuilder.filter(QueryBuilders.termQuery("taskName.keyword", jobAnalysis.getTaskName()));
        boolQueryBuilder.filter(QueryBuilders.termQuery("executionDate",
                DateUtil.timestampToUTCDate(jobAnalysis.getExecutionDate().getTime())));

        JobAnalysis jobAnalysisResult = search(JobAnalysis.class, index, boolQueryBuilder);

        String id;
        List<String> existedCategories = null;
        if (jobAnalysisResult == null || jobAnalysisResult.getDocId() == null) {
            id = UUID.randomUUID().toString();
            jobAnalysis.setCreateTime(new Date());
            jobAnalysis.setUpdateTime(new Date());
        } else {
            id = jobAnalysisResult.getDocId();
            if (jobAnalysisResult.getCategories() != null && jobAnalysisResult.getCategories().size() > 0) {
                existedCategories = jobAnalysisResult.getCategories();
            }
            jobAnalysis.setDocId(id);
        }

        if (existedCategories == null) {
            existedCategories = new ArrayList<>(categoryMap.keySet());
        } else {
            updateCategories(existedCategories, categoryMap);
        }
        jobAnalysis.setCategories(existedCategories);
        String data = JSON.toJSONString(jobAnalysis.genDoc());
        log.debug("updateJob:{},{}", id, data);
        api.upsertJson(client, index, id, data);

    }

    private void updateCategories(List<String> list, Map<String, Boolean> categoryMap) {
        list.forEach(categoryMap::remove);
        if (categoryMap.size() > 0) {
            categoryMap.forEach((k, v) -> list.add(k));
        }
    }

    /**
     * Update task-app categories information
     */
    public void updateTaskApp(TaskApp taskApp, Map<String, Boolean> categoryMap) throws Exception {
        String index = taskAppPrefix + DateUtil.formatToDay(taskApp.getExecutionDate());
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery("applicationId.keyword", taskApp.getApplicationId()));

        TaskApp taskAppResult = search(TaskApp.class, index, boolQueryBuilder);

        String id;
        List<String> existedCategories = null;
        if (taskAppResult == null || taskAppResult.getDocId() == null) {
            id = UUID.randomUUID().toString();
            taskApp.setCreateTime(new Date());

        } else {
            id = taskAppResult.getDocId();
            if (taskAppResult.getCategories() != null && taskAppResult.getCategories().size() > 0) {
                existedCategories = taskAppResult.getCategories();
            }
        }

        if (existedCategories == null) {
            existedCategories = new ArrayList<>(categoryMap.keySet());
        } else {
            updateCategories(existedCategories, categoryMap);
        }

        if (existedCategories.size() > 0) {
            taskApp.setDiagnoseResult("abnormal");
        }
        taskApp.setUpdateTime(new Date());
        taskApp.setCategories(existedCategories);
        UpdateApi api = new UpdateApi();
        String data = JSON.toJSONString(taskApp.genDoc());
        log.debug("updateTaskApp:{},{}", id, data);
        api.upsertJson(client, index, id, data);

    }

    public void saveGCReports(List<GCReport> gcReports, Date executionTime, String appId) {
        List<String> gcReportDocs = new ArrayList<>();
        for (GCReport gcReport : gcReports) {
            String data = JSON.toJSONString(gcReport);
            gcReportDocs.add(data);
        }
        String index = gcPrefix + DateUtil.formatToDay(executionTime);

        BulkResponse response;
        try {
            response = BulkApi.bulkJson(client, index, gcReportDocs);
        } catch (Exception e) {
            log.error("saveGCReportsErr:{},{}", appId, e);
            return;
        }
        BulkItemResponse[] responses = response.getItems();

        for (BulkItemResponse r : responses) {
            if (r.isFailed()) {
                log.info("saveGCReportsErr:{},{}", appId, r.getFailure().getCause());
            }
        }

        // save executor peak memory
        List<ExecutorPeakMemory> executorPeakList = new ArrayList<>();

        for (GCReport gcReport : gcReports) {
            executorPeakList.add(new ExecutorPeakMemory(gcReport.getExecutorId(), gcReport.getMaxHeapUsedSize(),
                    gcReport.getLogPath()));
        }
        Map<String, Object> m = new HashMap<>();
        m.put("applicationId", appId);
        m.put("executorPeak", executorPeakList);
        List<String> gcReportPeakDocs = new ArrayList<>();

        gcReportPeakDocs.add(JSON.toJSONString(m));

        BulkResponse gcPeakMemResponse;
        try {
            gcPeakMemResponse = BulkApi.bulkJson(client, index, gcReportPeakDocs);
        } catch (Exception e) {
            log.error("saveGCReportsErr:{},{}", appId, e);
            return;
        }
        BulkItemResponse[] gcPeakMemResponses = gcPeakMemResponse.getItems();

        for (BulkItemResponse r : gcPeakMemResponses) {
            if (r.isFailed()) {
                log.info("saveGCReportsErr:{},{}", appId, r.getFailure().getCause());
            }
        }
        log.info("saveGCReports:{}", appId);

    }

    private <T extends OpenSearchInfo> T search(Class<T> itemType, String index, BoolQueryBuilder boolQueryBuilder) throws Exception {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        log.info("es index:{}, search condition:{}", searchRequest.indices(), searchSourceBuilder.toString());

        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.warn("Exception:{}", e.getMessage());
        }

        T t = null;
        if (searchResponse != null) {
            for (SearchHit hit : searchResponse.getHits()) {
                t = JSON.parseObject(hit.getSourceAsString(), itemType);
                t.setDocId(hit.getId());
            }
        }

        return t;
    }

    public void saveTaskResults(LogRecord logRecord, List<TaskResult> taskResults) throws Exception {

        Map<String, Boolean> jobCategoryMap = new HashMap<>();
        Map<String, List<String>> appCategoryMap = new HashMap<>();
        for (TaskResult result : taskResults) {
            if (result.getCategories() != null) {
                for (String category : result.getCategories()) {
                    jobCategoryMap.put(category, true);
                }
                if (StringUtils.isNotBlank(result.getAppId())) {
                    List<String> categories = appCategoryMap.get(result.getAppId());
                    if (categories == null) {
                        appCategoryMap.put(result.getAppId(), result.getCategories());
                    } else {
                        categories.addAll(result.getCategories());
                    }
                }
            }
        }
        // update job categories
        if (jobCategoryMap.size() > 0) {
            log.info("updateJob:{},{}", logRecord.getId(), jobCategoryMap);
            try {
                OpenSearchWriter.getInstance().updateJob(logRecord.getJobAnalysis(), jobCategoryMap);
            } catch (Exception e) {
                log.error("updateJobErr:", e);
            }
        }

        for (Map.Entry<String, List<String>> item : appCategoryMap.entrySet()) {
            TaskApp taskApp = logRecord.getTaskApp(item.getKey());
            if (taskApp == null) {
                log.error("get {} taskApp null", item.getKey());
                continue;
            }
            List<String> categories = item.getValue();
            Map<String, Boolean> appCategories = new HashMap<>();
            categories.forEach(data -> appCategories.put(data, true));
            // update task-app categories
            log.info("updateTaskApp:{},{}", logRecord.getId(), appCategories);
            if (!appCategories.isEmpty()) {
                OpenSearchWriter.getInstance().updateTaskApp(taskApp, appCategories);
                continue;
            }
            if (logRecord.getIsOneClick()) {
                OpenSearchWriter.getInstance().updateTaskApp(taskApp, appCategories);
            }
        }
    }

    private enum OpenSearch {

        INSTANCE;

        private final OpenSearchWriter singleton;

        OpenSearch() {
            singleton = new OpenSearchWriter();
        }

        public OpenSearchWriter getInstance() {
            return singleton;
        }
    }
}
