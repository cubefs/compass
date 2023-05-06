package com.oppo.cloud.diagnosis.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.flink.FlinkJobs;
import com.oppo.cloud.common.domain.flink.FlinkTms;
import com.oppo.cloud.common.domain.flink.enums.RealtimeTaskAppState;
import com.oppo.cloud.common.domain.flink.enums.YarnApplicationState;
import com.oppo.cloud.diagnosis.config.ClusterConfig;
import com.oppo.cloud.diagnosis.config.FlinkYarnConfig;
import com.oppo.cloud.diagnosis.service.IClusterMetaService;
import com.oppo.cloud.diagnosis.service.FlinkMetaService;
import com.oppo.cloud.diagnosis.util.MemorySize;
import com.oppo.cloud.common.constant.YarnAppType;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.flink.JobManagerConfigItem;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.mapper.FlinkTaskMapper;
import com.oppo.cloud.mapper.TaskMapper;
import com.oppo.cloud.mapper.UserMapper;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实时元数据
 */
@Service
@Slf4j
public class FlinkMetaServiceImpl implements FlinkMetaService {

    /**
     * yarn api 获取集群作业
     */
    private static final String YARN_APP_URL = "http://%s/ws/v1/cluster/apps/%s";
    /**
     * flink api 获取配置
     */
    private static final String FLINK_JOB_MANAGER_CONFIG = "%s/jobmanager/config";
    /**
     * flink api 获取job
     */
    private static final String FLINK_JOBS = "%s/jobs";
    /**
     * flink api 获取tm
     */
    private static final String FLINK_TMS = "%s/taskmanagers";
    @Resource
    private ClusterConfig config;
    @Resource
    private IClusterMetaService iClusterMetaService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource(name = "restTemplate")
    private RestTemplate restTemplate;
    @Autowired
    public TaskMapper taskMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;
    @Autowired
    FlinkTaskMapper flinkTaskMapper;
    @Autowired
    FlinkYarnConfig flinkYarnConfig;
    /**
     * flink task type
     */
    private static final String TASK_TYPE_FLINK = "FLINK";

    public YarnApp requestYarnApp(String appId) {
        try {
            Map<String, String> clusters = iClusterMetaService.getYarnClusters();
            log.info(clusters.toString());
            for (Map.Entry<String, String> cluster : clusters.entrySet()) {
                String appUrl = String.format(YARN_APP_URL, cluster.getKey(), appId);
                ResponseEntity<String> responseEntity = null;
                log.info("请求app: {}", appUrl);
                responseEntity = restTemplate.getForEntity(appUrl, String.class);
                if (responseEntity.getBody() == null) {
                    continue;
                }
                JSONObject bodyJson = JSON.parseObject(responseEntity.getBody());
                String app = bodyJson.getString("app");
                YarnApp yarnApp = objectMapper.readValue(app, YarnApp.class);
                if (yarnApp != null) {
                    return yarnApp;
                }
            }
        } catch (Throwable e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public void saveRealtimeMetaOnYarn(TaskApplication taskApplication) {
        if (!taskApplication.getTaskType().equalsIgnoreCase(TASK_TYPE_FLINK)) {
            log.debug("不是flink作业");
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
        // 补充taskId, projectId信息
        Task task = getTask(taskApplication.getProjectName(), taskApplication.getFlowName(), taskApplication.getTaskName());
        if (task == null) {
            log.error("task is null :{}", taskApplication);
            return;
        }
        // 保存实时task元数据
        saveRealtimeTask(task);

        // 保存实时 app 元数据
        RealtimeTaskApp realtimeTaskApp;
        RealtimeTaskAppExample realtimeTaskAppExample = new RealtimeTaskAppExample();
        realtimeTaskAppExample.createCriteria()
                .andApplicationIdEqualTo(taskApplication.getApplicationId());
        List<RealtimeTaskApp> realtimeTaskApps = flinkTaskAppMapper.selectByExample(realtimeTaskAppExample);
        if (realtimeTaskApps == null || realtimeTaskApps.size() == 0) {
            realtimeTaskApp = new RealtimeTaskApp();
        } else if (realtimeTaskApps.size() == 1) {
            realtimeTaskApp = realtimeTaskApps.get(0);
        } else {
            realtimeTaskApp = realtimeTaskApps.get(0);
            log.error("realtimeTaskApps size 大于1 , appid:{}", taskApplication.getApplicationId());
        }
        // 保存实时task app
        saveRealtimeTaskApp(realtimeTaskApp, yarnApp, task, taskApplication);
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

    public void saveRealtimeTaskApp(RealtimeTaskApp realtimeTaskApp, YarnApp yarnApp, Task task,
                                    TaskApplication taskApplication) {
        User user = getUserById(task.getUserId());
        if (
                yarnApp.getState().equalsIgnoreCase(YarnApplicationState.FINISHED.getDesc()) ||
                        yarnApp.getState().equalsIgnoreCase(YarnApplicationState.FAILED.getDesc()) ||
                        yarnApp.getState().equalsIgnoreCase(YarnApplicationState.KILLED.getDesc())
        ) {
            realtimeTaskApp.setTaskState(RealtimeTaskAppState.FINISHED.getDesc());
        } else {
            realtimeTaskApp.setTaskState(RealtimeTaskAppState.RUNNING.getDesc());
        }
        // task meta
        realtimeTaskApp.setUsername(user.getUsername());
        realtimeTaskApp.setUserId(user.getUserId());
        realtimeTaskApp.setProjectName(task.getProjectName());
        realtimeTaskApp.setProjectId(task.getProjectId());
        realtimeTaskApp.setFlowName(task.getFlowName());
        realtimeTaskApp.setFlowId(task.getFlowId());
        realtimeTaskApp.setTaskName(task.getTaskName());
        realtimeTaskApp.setTaskId(task.getId());
        // task instance meta
        realtimeTaskApp.setExecutionTime(taskApplication.getExecuteTime());
        realtimeTaskApp.setTaskInstanceId(taskApplication.getId());
        realtimeTaskApp.setRetryTimes(taskApplication.getRetryTimes());
        // yarn app meta
        realtimeTaskApp.setApplicationId(yarnApp.getId());
        realtimeTaskApp.setFlinkTrackUrl(yarnApp.getTrackingUrl());
        realtimeTaskApp.setAllocatedMb(yarnApp.getAllocatedMB());
        realtimeTaskApp.setAllocatedVcores(yarnApp.getAllocatedVCores());
        realtimeTaskApp.setRunningContainers(yarnApp.getRunningContainers());
        realtimeTaskApp.setEngineType(yarnApp.getApplicationType());
        realtimeTaskApp.setDuration((double) yarnApp.getElapsedTime());
        realtimeTaskApp.setStartTime(new Date(yarnApp.getStartedTime()));
        realtimeTaskApp.setEndTime(new Date(yarnApp.getFinishedTime()));
        realtimeTaskApp.setVcoreSeconds((float) yarnApp.getVcoreSeconds());
        realtimeTaskApp.setMemorySeconds((float) yarnApp.getMemorySeconds());
        realtimeTaskApp.setQueue(yarnApp.getQueue());
        realtimeTaskApp.setClusterName(yarnApp.getClusterName());
        realtimeTaskApp.setExecuteUser(yarnApp.getUser());
        // flink meta
        List<JobManagerConfigItem> configItems = reqFlinkConfig(yarnApp);
        if (configItems != null) {
            String jobId = getJobId(yarnApp);
            fillFlinkMetaWithFlinkConfigOnYarn(realtimeTaskApp, configItems, jobId);
        } else {
            if (realtimeTaskApp.getId() == null) {
                log.error("flink config null {}", yarnApp);
                return;
            }
        }
        if (realtimeTaskApp.getCreateTime() == null) {
            realtimeTaskApp.setCreateTime(new Date());
        }
        realtimeTaskApp.setUpdateTime(new Date());
        if (realtimeTaskApp.getId() == null) {
            flinkTaskAppMapper.insertSelective(realtimeTaskApp);
        } else {
            flinkTaskAppMapper.updateByPrimaryKeySelective(realtimeTaskApp);
        }
    }

    public List<String> getTmIds(YarnApp yarnApp) {
        String trackingUrl = yarnApp.getTrackingUrl();
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

    public String getJobId(YarnApp yarnApp) {
        return getJobId(yarnApp.getTrackingUrl());
    }

    public String getJobId(String trackingUrl) {
        String jobsUrl = String.format(FLINK_JOBS, trackingUrl);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(jobsUrl, String.class);
            if (responseEntity.getBody() == null) {
                log.error("flink api:{} body is null", jobsUrl);
                return null;
            }
            FlinkJobs overview;
            overview = JSON.parseObject(responseEntity.getBody(), FlinkJobs.class);
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

    public List<JobManagerConfigItem> reqFlinkConfig(YarnApp yarnApp) {
        String trackingUrl = yarnApp.getTrackingUrl();
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
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void fillFlinkMetaWithFlinkConfigOnYarn(RealtimeTaskApp realtimeTaskApp, List<JobManagerConfigItem> configItems, String jobId) {
        try {

            // 找资源参数
            for (JobManagerConfigItem jobManagerConfigItem : configItems) {
                if (flinkYarnConfig.getParallel().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    realtimeTaskApp.setParallel(Integer.valueOf(jobManagerConfigItem.getValue()));
                }
                if (flinkYarnConfig.getTmSlot().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    realtimeTaskApp.setTmSlot(Integer.valueOf(jobManagerConfigItem.getValue()));
                }
                if (flinkYarnConfig.getTmSlot().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    realtimeTaskApp.setTmCore(Integer.valueOf(jobManagerConfigItem.getValue()));
                }
                if (flinkYarnConfig.getTmMemory().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    int tmMemMb = MemorySize.parse(jobManagerConfigItem.getValue())
                            .getMebiBytes();
                    realtimeTaskApp.setTmMem(tmMemMb);
                }
                if (flinkYarnConfig.getJmMemory().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    int jmMemMb = MemorySize.parse(jobManagerConfigItem.getValue())
                            .getMebiBytes();
                    realtimeTaskApp.setJmMem(jmMemMb);
                }
                if (flinkYarnConfig.getJobName().equalsIgnoreCase(jobManagerConfigItem.getKey())) {
                    realtimeTaskApp.setJobName(jobManagerConfigItem.getValue());
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    public void saveRealtimeTask(Task task) {
        RealtimeTaskExample realtimeTaskExample = new RealtimeTaskExample();
        realtimeTaskExample.createCriteria()
                .andTaskIdEqualTo(task.getId());
        List<FlinkTask> flinkTaskApps = flinkTaskMapper.selectByExample(realtimeTaskExample);
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
