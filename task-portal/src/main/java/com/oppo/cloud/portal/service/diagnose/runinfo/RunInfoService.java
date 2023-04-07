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

package com.oppo.cloud.portal.service.diagnose.runinfo;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.elasticsearch.TaskApp;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.domain.diagnose.DiagnoseReport;
import com.oppo.cloud.portal.domain.diagnose.info.AppInfo;
import com.oppo.cloud.portal.domain.diagnose.info.ClusterInfo;
import com.oppo.cloud.portal.domain.diagnose.info.TaskInfo;
import com.oppo.cloud.portal.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

@Service
public class RunInfoService {

    @Autowired
    ElasticSearchService elasticSearchService;

    @Value(value = "${custom.elasticsearch.logIndex.name}")
    String logIndex;

    @Value(value = "${custom.elasticsearch.appIndex.name}")
    String appIndex;

    /**
     * 产生诊断报告的运行信息
     * @param detectorStorage
     * @return
     */
    public DiagnoseReport.RunInfo generateRunInfo(DetectorStorage detectorStorage) {
        DiagnoseReport.RunInfo runInfo = new DiagnoseReport.RunInfo();
        ClusterInfo clusterInfo = new ClusterInfo();
        TaskInfo taskInfo = new TaskInfo();
        HashMap<String, Object> termQuery = new HashMap<>();
        termQuery.put("applicationId.keyword", detectorStorage.getApplicationId());
        try {
            runInfo.setClusterInfo(clusterInfo);
            runInfo.setTaskInfo(taskInfo);
            List<TaskApp> taskApps = elasticSearchService.find(TaskApp.class, termQuery, appIndex + "-*");
            if (taskApps.size() == 0) {
                return runInfo;
            }
            TaskApp taskApp = taskApps.get(0);
            for (TaskApp taskAppTemp : taskApps) {
                if (taskAppTemp.getCategories() != null
                        && taskAppTemp.getCategories().size() > taskApp.getCategories().size()) {
                    taskApp = taskAppTemp;
                }
            }
            if (taskApp.getCategories() == null) {
                taskApp.setCategories(new ArrayList<>());
            }
            clusterInfo.setClusterName(taskApp.getClusterName());
            clusterInfo.setExecuteUser(taskApp.getExecuteUser());
            clusterInfo.setExecuteQueue(taskApp.getQueue());
            clusterInfo.setSparkUi(taskApp.getSparkUI());
            taskInfo.setExecutionTime(DateUtil.format(taskApp.getExecutionDate()));
            taskInfo.setTaskName(taskApp.getTaskName());
            taskInfo.setFlowName(taskApp.getFlowName());
            taskInfo.setProjectName(taskApp.getProjectName());
            taskInfo.setMemorySeconds(String.format("%.2f GB·s", taskApp.getMemorySeconds() / 1024 ));
            taskInfo.setVcoreSeconds(String.format("%.2f vcore·s", taskApp.getVcoreSeconds()));
            taskInfo.setAppTime(
                    DateUtil.timeSimplify(((taskApp.getFinishTime() == null ? 0 : taskApp.getFinishTime().getTime())
                            - (taskApp.getStartTime() == null ? 0 : taskApp.getStartTime().getTime())) / 1000.0));
            taskInfo.setApplicationId(taskApp.getApplicationId());
            if (taskApp.getCategories() == null || taskApp.getCategories().size() == 0) {
                taskInfo.setCategories(Collections.singletonList("正常"));
            } else {
                taskInfo.setCategories(AppCategoryEnum.getAppCategoryCh(taskApp.getCategories()));
            }
            AppInfo appInfo = generateAppInfo(detectorStorage.getEnv());
            runInfo.setAppInfo(appInfo);
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            runInfo.setError(baos.toString());
        }
        return runInfo;
    }

    /**
     * 产生spark配置信息
     * @param env
     * @return
     */
    private AppInfo generateAppInfo(Map<String, Object> env) {
        AppInfo appInfo = new AppInfo();
        for (String name : env.keySet()) {
            String value = (String) env.get(name);
            switch (name) {
                case "spark.executor.memory":
                    appInfo.setExecutorMemory(value);
                    break;
                case "spark.driver.memory":
                    appInfo.setDriverMemory(value);
                    break;
                case "spark.driver.memoryOverhead":
                    appInfo.setDriverOverhead(value);
                    break;
                case "spark.executor.memoryOverhead":
                    appInfo.setExecutorOverhead(value);
                    break;
                case "spark.default.parallelism":
                    appInfo.setParallelism(value);
                    break;
                case "spark.executor.cores":
                    appInfo.setExecutorCores(value);
                    break;
                case "spark.dynamicAllocation.maxExecutors":
                    appInfo.setMaxExecutors(value);
                    break;
                case "spark.sql.shuffle.partitions":
                    appInfo.setShufflePartitions(value);
                    break;
                default:
            }
        }
        return appInfo;
    }

}
