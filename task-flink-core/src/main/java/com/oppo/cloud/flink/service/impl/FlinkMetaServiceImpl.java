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

package com.oppo.cloud.flink.service.impl;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.constant.YarnAppFinalStatus;
import com.oppo.cloud.common.constant.YarnAppType;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.flink.FlinkJobs;
import com.oppo.cloud.common.domain.flink.FlinkTms;
import com.oppo.cloud.common.domain.flink.JobManagerConfigItem;
import com.oppo.cloud.common.domain.flink.enums.FlinkTaskAppState;
import com.oppo.cloud.common.domain.flink.enums.YarnApplicationState;
import com.oppo.cloud.flink.config.FlinkYarnConfig;
import com.oppo.cloud.flink.service.FlinkMetaService;
import com.oppo.cloud.flink.util.MemorySize;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.mapper.FlinkTaskMapper;
import com.oppo.cloud.mapper.TaskMapper;
import com.oppo.cloud.mapper.UserMapper;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Real-time metadata
 */
@Service
@Slf4j
public class FlinkMetaServiceImpl implements FlinkMetaService {

    /**
     * Get cluster jobs with YARN API
     */
    private static final String YARN_APP_URL = "http://%s/ws/v1/cluster/apps/%s";
    /**
     * Flink API configuration retrieval
     */
    private static final String FLINK_JOB_MANAGER_CONFIG = "%s/jobmanager/config";
    /**
     * Flink API job information retrieval
     */
    private static final String FLINK_JOBS = "%s/jobs";
    /**
     * Flink API taskmanager information retrieval
     */
    private static final String FLINK_TMS = "%s/taskmanagers";


    @Resource(name = "flinkRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    public TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FlinkTaskAppMapper flinkTaskAppMapper;

    @Autowired
    private FlinkTaskMapper flinkTaskMapper;

    @Autowired
    private FlinkYarnConfig flinkYarnConfig;

    @Value("${custom.opensearch.yarnIndex.name}")
    private String yarnAppIndex;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /**
     * flink task type
     */
    private static final String TASK_TYPE_FLINK = "FLINK";

    public SearchSourceBuilder genSearchBuilder(Map<String, Object> termQuery, Map<String, Object[]> rangeConditions,
                                                Map<String, SortOrder> sort,
                                                Map<String, Object> or) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        // Query Conditions
        for (String key : termQuery.keySet()) {
            Object value = termQuery.get(key);
            if (value == null) {
                // null value query
                boolQuery.mustNot(QueryBuilders.existsQuery(key));
            } else if ("".equals(value)) {
                // No valid string matches.
                boolQuery.mustNot(QueryBuilders.wildcardQuery(key, "*"));
            } else if (value instanceof java.util.List) {
                // List Query
                boolQuery.filter(QueryBuilders.termsQuery(key, (List<String>) value));
            } else {
                // Single String Query
                boolQuery.filter(QueryBuilders.termsQuery(key, value));
            }
        }
        // Or condition query[xx and (a=1 or c=2)]
        if (or != null) {
            BoolQueryBuilder orQuery = new BoolQueryBuilder();
            for (String key : or.keySet()) {
                Object value = or.get(key);
                if (value != null) {
                    orQuery.should(QueryBuilders.termQuery(key, value));
                }
            }
            boolQuery.must(orQuery);
        }
        // Range Query
        if (rangeConditions != null) {
            for (String key : rangeConditions.keySet()) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(key);
                Object[] queryValue = rangeConditions.get(key);
                if (queryValue[0] != null) {
                    rangeQueryBuilder.gte(queryValue[0]);
                }
                if (queryValue[1] != null) {
                    rangeQueryBuilder.lte(queryValue[1]);
                }
                boolQuery.filter(rangeQueryBuilder);
            }
        }
        // sort
        if (sort != null) {
            for (String key : sort.keySet()) {
                builder.sort(key, sort.get(key));
            }
        }
        builder.query(boolQuery);
        return builder;
    }

    public SearchHits find(SearchSourceBuilder builder, String... indexes) throws Exception {
        SearchRequest searchRequest = new SearchRequest().indices(indexes).source(builder);
        Long startTime = System.currentTimeMillis();
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Long endTime = System.currentTimeMillis();
        log.info("indexes:{}, duration:{} ,condition:{}", indexes, (endTime - startTime) / 1000, builder.toString());
        return searchResponse.getHits();
    }

    public YarnApp requestYarnApp(String applicationId) {
        try {
            YarnApp yarnApp = null;
            HashMap<String, Object> termQuery = new HashMap<>();
            termQuery.put("id.keyword", applicationId);
            SearchSourceBuilder searchSourceBuilder = this.genSearchBuilder(termQuery, null, null, null);
            SearchHits searchHits = this.find(searchSourceBuilder, yarnAppIndex + "-*");
            if (searchHits.getHits().length == 0) {
                log.info("can not find this appId from yarnApp, appId:{}", applicationId);
                return null;
            }
            for (SearchHit hit : searchHits) {
                yarnApp = JSON.parseObject(hit.getSourceAsString(), YarnApp.class);
            }
            if (yarnApp == null) {
                log.info("yarnApp is null, appId:{}", applicationId);
                return null;
            }
            if (yarnApp.getFinalStatus().equals(YarnAppFinalStatus.SUCCEEDED.toString()) ||
                    yarnApp.getFinalStatus().equals(YarnAppFinalStatus.FAILED.toString()) ||
                    yarnApp.getFinalStatus().equals(YarnAppFinalStatus.KILLED.toString())) {
                return yarnApp;
            }
            log.info("yarnApp state:{}, finalStatus:{}, appId:{}", yarnApp.getState(),
                    yarnApp.getFinalStatus(), applicationId);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
    }


    @Override
    public void saveRealtimeMetaOnYarn(TaskApplication taskApplication) {
        if (!taskApplication.getTaskType().equalsIgnoreCase(TASK_TYPE_FLINK)) {
            log.debug("Not a Flink Job.");
            return;
        }
        YarnApp yarnApp = requestYarnApp(taskApplication.getApplicationId());
        if (yarnApp == null) {
            log.info("yarn app null {}", taskApplication.getApplicationId());
            return;
        }
        if (!YarnAppType.FLINK.getMsg().equalsIgnoreCase(yarnApp.getApplicationType())) {
            log.debug("not a flink app task:{} yarn app:{} ", taskApplication, yarnApp);
            return;
        }
        // Provide taskId and projectId information
        Task task = getTask(taskApplication.getProjectName(), taskApplication.getFlowName(), taskApplication.getTaskName());
        if (task == null) {
            log.error("task is null :{}", taskApplication);
            return;
        }
        // Save Real-time Task Metadata.
        saveRealtimeTask(task);

        // Save Real-time App Metadata.
        FlinkTaskApp flinkTaskApp;
        FlinkTaskAppExample flinkTaskAppExample = new FlinkTaskAppExample();
        flinkTaskAppExample.createCriteria()
                .andApplicationIdEqualTo(taskApplication.getApplicationId());
        List<FlinkTaskApp> flinkTaskApps = flinkTaskAppMapper.selectByExample(flinkTaskAppExample);
        if (flinkTaskApps == null || flinkTaskApps.size() == 0) {
            flinkTaskApp = new FlinkTaskApp();
        } else if (flinkTaskApps.size() == 1) {
            flinkTaskApp = flinkTaskApps.get(0);
        } else {
            flinkTaskApp = flinkTaskApps.get(0);
            log.error("realtimeTaskApps size > 1 , appid:{}", taskApplication.getApplicationId());
        }
        // Save Real-time Task App.
        saveRealtimeTaskApp(flinkTaskApp, yarnApp, task, taskApplication);
    }


    public Task getTask(String projectName, String flowName, String taskName) {
        TaskExample taskExample = new TaskExample();
        taskExample.createCriteria().andTaskNameEqualTo(taskName)
                .andFlowNameEqualTo(flowName)
                .andProjectNameEqualTo(projectName);
        List<Task> tasks = taskMapper.selectByExample(taskExample);
        if (tasks.size() > 0) {
            return tasks.get(0);
        }
        return null;
    }

    public User getUserById(Integer id) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andUserIdEqualTo(id);
        List<User> users = userMapper.selectByExample(userExample);
        if (users != null && users.size() > 0) {
            User user = users.get(0);
            return user;
        }
        return null;
    }

    public void saveRealtimeTaskApp(FlinkTaskApp flinkTaskApp, YarnApp yarnApp, Task task,
                                    TaskApplication taskApplication) {
        User user = getUserById(task.getUserId());
        if (
                yarnApp.getState().equalsIgnoreCase(YarnApplicationState.FINISHED.getDesc()) ||
                        yarnApp.getState().equalsIgnoreCase(YarnApplicationState.FAILED.getDesc()) ||
                        yarnApp.getState().equalsIgnoreCase(YarnApplicationState.KILLED.getDesc())
        ) {
            flinkTaskApp.setTaskState(FlinkTaskAppState.FINISHED.getDesc());
        } else {
            flinkTaskApp.setTaskState(FlinkTaskAppState.RUNNING.getDesc());
        }
        // task meta
        flinkTaskApp.setUsername(user.getUsername());
        flinkTaskApp.setUserId(user.getUserId());
        flinkTaskApp.setProjectName(task.getProjectName());
        flinkTaskApp.setProjectId(task.getProjectId());
        flinkTaskApp.setFlowName(task.getFlowName());
        flinkTaskApp.setFlowId(task.getFlowId());
        flinkTaskApp.setTaskName(task.getTaskName());
        flinkTaskApp.setTaskId(task.getId());
        // task instance meta
        flinkTaskApp.setExecutionTime(taskApplication.getExecuteTime());
        flinkTaskApp.setTaskInstanceId(taskApplication.getId());
        flinkTaskApp.setRetryTimes(taskApplication.getRetryTimes());
        // yarn app meta
        flinkTaskApp.setApplicationId(yarnApp.getId());
        flinkTaskApp.setFlinkTrackUrl(yarnApp.getTrackingUrl());
        flinkTaskApp.setAllocatedMb(yarnApp.getAllocatedMB());
        flinkTaskApp.setAllocatedVcores(yarnApp.getAllocatedVCores());
        flinkTaskApp.setRunningContainers(yarnApp.getRunningContainers());
        flinkTaskApp.setEngineType(yarnApp.getApplicationType());
        flinkTaskApp.setDuration((double) yarnApp.getElapsedTime());
        flinkTaskApp.setStartTime(new Date(yarnApp.getStartedTime()));
        flinkTaskApp.setEndTime(new Date(yarnApp.getFinishedTime()));
        flinkTaskApp.setVcoreSeconds((float) yarnApp.getVcoreSeconds());
        flinkTaskApp.setMemorySeconds((float) yarnApp.getMemorySeconds());
        flinkTaskApp.setQueue(yarnApp.getQueue());
        flinkTaskApp.setClusterName(yarnApp.getClusterName());
        flinkTaskApp.setExecuteUser(yarnApp.getUser());
        // flink meta
        List<JobManagerConfigItem> configItems = reqFlinkConfig(flinkTaskApp.getFlinkTrackUrl());
        if (configItems != null) {
            String jobId = getJobId(flinkTaskApp.getFlinkTrackUrl());
            fillFlinkMetaWithFlinkConfigOnYarn(flinkTaskApp, configItems, jobId);
        } else {
            if (flinkTaskApp.getId() == null) {
                log.error("flink config null {}", yarnApp);
                return;
            }
        }
        if (flinkTaskApp.getCreateTime() == null) {
            flinkTaskApp.setCreateTime(new Date());
        }
        flinkTaskApp.setUpdateTime(new Date());
        if (flinkTaskApp.getId() == null) {
            flinkTaskAppMapper.insertSelective(flinkTaskApp);
        } else {
            flinkTaskAppMapper.updateByPrimaryKeySelective(flinkTaskApp);
        }
    }

    @Override
    public List<String> getTmIds(String trackingUrl) {
        String tmsUrl = String.format(FLINK_TMS, trackingUrl);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(tmsUrl, String.class);
            if (responseEntity.getBody() == null) {
                log.error("flink api:{} body is null", tmsUrl);
                return null;
            }
            FlinkTms tms;
            tms = JSON.parseObject(responseEntity.getBody(), FlinkTms.class);
            if (tms != null && tms.getTaskmanagers() != null && tms.getTaskmanagers().size() > 0) {
                return tms.getTaskmanagers().stream()
                        .map(FlinkTms.FlinkTmsTaskManager::getId).collect(Collectors.toList());
            } else {
                return null;
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get jobId through Flink Tracking URL.
     *
     * @param trackingUrl
     * @return
     */
    @Override
    public String getJobId(String trackingUrl) {
        String jobsUrl = String.format(FLINK_JOBS, trackingUrl);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(jobsUrl, String.class);
            if (responseEntity.getBody() == null) {
                log.error("flink api:{} body is null", jobsUrl);
                return null;
            }
            FlinkJobs overview = JSON.parseObject(responseEntity.getBody(), FlinkJobs.class);
            if (overview != null && overview.getJobs() != null && overview.getJobs().size() > 0) {
                return overview.getJobs().get(0).getId();
            } else {
                return null;
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<JobManagerConfigItem> reqFlinkConfig(String trackingUrl) {
        String jobManagerConfigUrl = String.format(FLINK_JOB_MANAGER_CONFIG, trackingUrl);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(jobManagerConfigUrl, String.class);
            if (responseEntity.getBody() == null) {
                log.error("flink api:{} body is null", jobManagerConfigUrl);
                return null;
            }
            List<JobManagerConfigItem> configItems;
            configItems = JSON.parseArray(responseEntity.getBody(), JobManagerConfigItem.class);
            return configItems;
        } catch (Throwable e) {
            log.error(e.getMessage() + jobManagerConfigUrl, e);
            return null;
        }
    }

    @Override
    public void fillFlinkMetaWithFlinkConfigOnYarn(FlinkTaskApp flinkTaskApp, List<JobManagerConfigItem> configItems, String jobId) {
        try {

            // Find resource parameters.
            for (JobManagerConfigItem jobManagerConfigItem : configItems) {
                if (flinkYarnConfig.getParallel().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    flinkTaskApp.setParallel(Integer.valueOf(jobManagerConfigItem.getValue()));
                }
                if (flinkYarnConfig.getTmSlot().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    flinkTaskApp.setTmSlot(Integer.valueOf(jobManagerConfigItem.getValue()));
                }
                if (flinkYarnConfig.getTmSlot().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    flinkTaskApp.setTmCore(Integer.valueOf(jobManagerConfigItem.getValue()));
                }
                if (flinkYarnConfig.getTmMemory().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    int tmMemMb = MemorySize.parse(jobManagerConfigItem.getValue())
                            .getMebiBytes();
                    flinkTaskApp.setTmMem(tmMemMb);
                }
                if (flinkYarnConfig.getJmMemory().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    int jmMemMb = MemorySize.parse(jobManagerConfigItem.getValue())
                            .getMebiBytes();
                    flinkTaskApp.setJmMem(jmMemMb);
                }
                if (flinkYarnConfig.getJobName().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    flinkTaskApp.setJobName(jobManagerConfigItem.getValue());
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    public void saveRealtimeTask(Task task) {
        FlinkTaskExample flinkTaskExample = new FlinkTaskExample();
        flinkTaskExample.createCriteria()
                .andTaskIdEqualTo(task.getId());
        List<FlinkTask> flinkTaskApps = flinkTaskMapper.selectByExample(flinkTaskExample);
        if (flinkTaskApps.size() > 1) {
            log.error("realtimeTaskApps size > 1 id : {}", task.getId());
        }
        FlinkTask rt;
        if (flinkTaskApps.size() == 0) {
            rt = new FlinkTask();
        } else {
            rt = flinkTaskApps.get(0);
        }
        User user = getUserById(task.getUserId());
        rt.setUsername(user.getUsername());
        rt.setUserId(user.getUserId());
        rt.setProjectName(task.getProjectName());
        rt.setProjectId(task.getProjectId());
        rt.setFlowName(task.getFlowName());
        rt.setFlowId(task.getFlowId());
        rt.setTaskName(task.getTaskName());
        rt.setTaskId(task.getId());
        rt.setCreateTime(task.getCreateTime());
        rt.setUpdateTime(task.getUpdateTime());
        if (rt.getId() == null) {
            flinkTaskMapper.insertSelective(rt);
        } else {
            flinkTaskMapper.updateByPrimaryKeySelective(rt);
        }
    }
}
