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

package com.oppo.cloud.portal.controller;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.LogSummary;
import com.oppo.cloud.common.domain.opensearch.SimpleUser;
import com.oppo.cloud.common.domain.opensearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.dao.TaskInstanceExtendMapper;
import com.oppo.cloud.portal.service.OpenSearchService;
import org.opensearch.script.Script;
import org.opensearch.search.aggregations.AggregationBuilder;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
class JobControllerTest {

    @Value(value = "${custom.opensearch.jobIndex.name}")
    private String jobIndex;

    @Value(value = "${custom.opensearch.appIndex.name}")
    private String appIndex;

    @Value(value = "${custom.opensearch.logIndex.name}")
    private String logIndex;

    @Value(value = "${custom.opensearch.detectIndex.name}")
    private String detectIndex;

    @Autowired
    OpenSearchService openSearchService;

    @Autowired
    TaskInstanceExtendMapper taskInstanceExtendMapper;

    @Test
    public void searchJob() throws Exception {
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("_id", "7aa3fe22-2b34-4dd5-aa45-1ba84f1f7cf9");
        List<JobAnalysis> jobAnalyses =
                openSearchService.find(JobAnalysis.class, termQuery, "compass-job-analysis-2022-11-22");
        System.out.println(jobAnalyses);
    }

    @Test
    public void addJob() throws Exception {
        JobAnalysis jobAnalysis = new JobAnalysis();
        jobAnalysis.setProjectName("oflow-test");
        jobAnalysis.setProjectId(1);
        jobAnalysis.setFlowName("os_daily_crms_guard_tasks");
        jobAnalysis.setFlowId(1);
        jobAnalysis.setTaskName("rpt_crms_charge_job_temp_cnt_d");
        jobAnalysis.setTaskId(1);
        jobAnalysis.setExecutionDate(new Date(1669046400 * 1000L));
        jobAnalysis.setStartTime(new Date(1665331200 * 1000L));
        jobAnalysis.setEndTime(new Date(1665331200 * 1000L));
        jobAnalysis.setDuration(10d);
        jobAnalysis.setTaskState("success");
        jobAnalysis.setMemorySeconds(100d);
        jobAnalysis.setVcoreSeconds(200d);
        jobAnalysis.setRetryTimes(10);
        jobAnalysis.setCategories(Arrays.asList(JobCategoryEnum.executionFailed.name(),
                AppCategoryEnum.CPU_WASTE.getCategory(), AppCategoryEnum.OTHER_EXCEPTION.getCategory(),
                AppCategoryEnum.SPECULATIVE_TASK.getCategory(), AppCategoryEnum.JOB_DURATION.getCategory()));
        jobAnalysis.setEndTimeBaseline("0~2022-10-11 05:00:21");
        jobAnalysis.setSuccessExecutionDay("2022-10-11 05:00:21");
        jobAnalysis.setSuccessDays("10");
        jobAnalysis.setMemory(100.0);
        jobAnalysis.setMemoryRatio(100.0);
        jobAnalysis.setDeleted(1);
        jobAnalysis.setTaskStatus(1);
        jobAnalysis.setCreateTime(new Date(1665331200 * 1000L));
        jobAnalysis.setUpdateTime(new Date(1665331200 * 1000L));
        List<SimpleUser> simpleUsers = new ArrayList<>();
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setUsername("王杰");
        simpleUser.setUserId(1);
        simpleUsers.add(simpleUser);
        SimpleUser simpleUser1 = new SimpleUser();
        simpleUser1.setUsername("李四");
        simpleUser1.setUserId(2);
        simpleUsers.add(simpleUser1);
        jobAnalysis.setUsers(simpleUsers);
        String indexName = jobIndex + "-";
        indexName += DateUtil.format(jobAnalysis.getExecutionDate(), "yyyy-MM-dd");
        Map<String, Object> map = jobAnalysis.genDoc();
        System.out.println(map);
        openSearchService.insertOrUpDate(indexName, UUID.randomUUID().toString(), jobAnalysis.genDoc());
    }

    @Test
    public void addJobApp() throws Exception {
        TaskApp taskApp = new TaskApp();
        taskApp.setApplicationId("appid");
        taskApp.setApplicationType("spark");
        taskApp.setExecuteUser("hive");
        taskApp.setQueue("queue");
        taskApp.setClusterName("clusterName");
        List<SimpleUser> simpleUsers = new ArrayList<>();
        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setUsername("王杰");
        simpleUser.setUserId(1);
        simpleUsers.add(simpleUser);
        SimpleUser simpleUser1 = new SimpleUser();
        simpleUser1.setUsername("李四");
        simpleUser1.setUserId(2);
        simpleUsers.add(simpleUser1);
        taskApp.setUsers(simpleUsers);
        taskApp.setProjectName("oflow-test");
        taskApp.setProjectId(1);
        taskApp.setFlowName("os_daily_crms_guard_tasks");
        taskApp.setFlowId(1);
        taskApp.setTaskName("rpt_crms_charge_job_temp_cnt_d");
        taskApp.setTaskId(1);
        taskApp.setExecutionDate(new Date(1665331200 * 1000L));
        taskApp.setStartTime(new Date(1665331200 * 1000L));
        taskApp.setFinishTime(new Date(1665331200 * 1000L));
        taskApp.setElapsedTime(10d);
        taskApp.setTaskAppState("success");
        taskApp.setMemorySeconds(100d);
        taskApp.setVcoreSeconds(100d);
        taskApp.setDiagnostics("测试");
        taskApp.setSparkUI("http://localhost:18080");
        taskApp.setCreateTime(new Date(1665331200 * 1000L));
        taskApp.setUpdateTime(new Date(1665331200 * 1000L));
        taskApp.setRetryTimes(1);
        taskApp.setDiagnoseResult("abnormal");
        taskApp.setCategories(
                Arrays.asList(AppCategoryEnum.CPU_WASTE.getCategory(), AppCategoryEnum.OTHER_EXCEPTION.getCategory(),
                        AppCategoryEnum.SPECULATIVE_TASK.getCategory(), AppCategoryEnum.JOB_DURATION.getCategory()));
        String appIndexName = appIndex + "-";
        appIndexName += DateUtil.format(taskApp.getExecutionDate(), "yyyy-MM-dd");
        Map<String, Object> map = taskApp.genDoc();
        System.out.println(map);
        openSearchService.insertOrUpDate(appIndexName, "352c06de-a313-4e60-965a-5b2e3705b198", map);
    }

    @Test
    public void insertLogToEs() throws Exception {
        LogSummary logSummary = new LogSummary();
        logSummary.setLogType("scheduler");
        logSummary.setAction("otherError");
        logSummary.setRawLog(
                "User class threw exception: java.sql.SQLException: Access denied for user 'ads_da_user'@'10.39.10" +
                        ".168' (using password: YES)\n" +
                        "at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:129)\n" +
                        "at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:97)\n" +
                        "at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping" +
                        ".java:122)\n" +
                        "at com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:836)\n" +
                        "at com.mysql.cj.jdbc.ConnectionImpl.(ConnectionImpl.java:456)\n" +
                        "at com.mysql.cj.jdbc.ConnectionImpl.getInstance(ConnectionImpl.java:246)\n" +
                        "at com.mysql.cj.jdbc.NonRegisteringDriver.connect(NonRegisteringDriver.java:197)\n" +
                        "at org.apache.spark.sql.execution.datasources.jdbc.DriverWrapper.connect(DriverWrapper.scala:45)\n"
                        +
                        "at org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils$$anonfun$createConnectionFactory$1"
                        +
                        ".apply(JdbcUtils.scala:63)\n" +
                        "at org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils$$anonfun$createConnectionFactory$1"
                        +
                        ".apply(JdbcUtils.scala:54)\n" +
                        "at org.apache.spark.sql.execution.datasources.jdbc.JDBCRDD$.resolveTable(JDBCRDD.scala:56)\n" +
                        "at org.apache.spark.sql.execution.datasources.jdbc.JDBCRelation$.getSchema(JDBCRelation.scala:210)\n"
                        +
                        "at org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider.createRelation" +
                        "(JdbcRelationProvider.scala:35)\n" +
                        "at org.apache.spark.sql.execution.datasources.DataSource.resolveRelation(DataSource.scala:318)\n"
                        +
                        "at org.apache.spark.sql.DataFrameReader.loadV1Source(DataFrameReader.scala:223)\n" +
                        "at org.apache.spark.sql.DataFrameReader.load(DataFrameReader.scala:211)\n" +
                        "at org.apache.spark.sql.DataFrameReader.load(DataFrameReader.scala:167)\n" +
                        "at com.oppo.ads.scala.action.redis.MysqlToRedisSingleTable$$anonfun$main$1.apply$mcVI$sp" +
                        "(MysqlToRedisSingleTable.scala:77)\n" +
                        "at scala.collection.immutable.Range.foreach$mVc$sp(Range.scala:160)\n" +
                        "at com.oppo.ads.scala.action.redis.MysqlToRedisSingleTable$.main(MysqlToRedisSingleTable.scala:55)\n"
                        +
                        "at com.oppo.ads.scala.action.redis.MysqlToRedisSingleTable.main(MysqlToRedisSingleTable.scala)\n"
                        +
                        "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
                        "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
                        "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
                        "at java.lang.reflect.Method.invoke(Method.java:498)\n" +
                        "at org.apache.spark.deploy.yarn.ApplicationMaster$$anon$2.run(ApplicationMaster.scala:678)");
        logSummary.setProjectName("test_lx");
        logSummary.setFlowName("failed_test");
        logSummary.setTaskName("node_failed");
        logSummary.setExecutionDate(DateUtil.parseStrToDate("2022-10-24 14:50:46"));
        logSummary.setRetryTimes(0);
        logSummary.setApplicationId("");
        logSummary.setStep(0);
        logSummary.setLogTimestamp(1666713600);
        openSearchService.insertOrUpDate(logIndex + "-" + "2022-10-26", UUID.randomUUID().toString(),
                logSummary.genDoc());
    }

    @Test
    public void updateTaskAppEs() throws Exception {
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId", "application_1662709492856_0271_1");
        List<TaskApp> taskAppList = openSearchService.find(TaskApp.class, termQuery, appIndex + "-*");
        for (TaskApp taskApp : taskAppList) {
            taskApp.setApplicationId("application_1662709492856_0271_1");
            taskApp.setCategories(
                    Arrays.asList("otherException", "stageDurationAbnormal", "speculativeTask", "cpuWaste"));
            openSearchService.insertOrUpDate(taskApp.genIndex(appIndex), taskApp.genDocId(), taskApp.genDoc());
            break;
        }
    }

    @Test
    public void updateDetectorTaskAppEs() throws Exception {
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId", "application_1662709492856_0271_1");
        List<DetectorStorage> detectorStorageList =
                openSearchService.find(DetectorStorage.class, termQuery, detectIndex + "-*");
        for (DetectorStorage detectorStorage : detectorStorageList) {
            for (DetectorResult detectorResult : detectorStorage.getDataList()) {
                if (detectorResult.getAppCategory().equals(AppCategoryEnum.LARGE_TABLE_SCAN.getCategory())) {
                    detectorResult.setAbnormal(false);
                }
            }
            openSearchService.insertOrUpDate("compass-detector-app-2022-11-07",
                    "bbde0eda-8888-4198-bda9-a92d4723ce31", detectorStorage);
            break;
        }
    }

    @Test
    public void testTaskInstance() throws Exception {
        Integer value = taskInstanceExtendMapper.searchJobCount(new Date(), new Date());
        System.out.println(value);
    }

    @Test
    public void testSearchJob() throws Exception {
        SearchSourceBuilder searchSourceBuilder = openSearchService.genSearchBuilder(null, null, null, null);
        AggregationBuilder aggregationBuilderAbnormalJobCount = AggregationBuilders.terms("groupByCount")
                .script(new Script(
                        "doc['projectName.keyword'].value+'@@'+doc['flowName.keyword'].value+'@@'+doc['taskName.keyword'].value"))
                .size(10000);
        searchSourceBuilder.aggregation(aggregationBuilderAbnormalJobCount);
        Aggregations aggregationsGroupByCount =
                openSearchService.findRawAggregations(searchSourceBuilder, jobIndex + "-*");
        Terms terms = aggregationsGroupByCount.get("groupByCount");
        int abnormalJobCount = terms.getBuckets().size();
        System.out.println(abnormalJobCount);
    }
}
