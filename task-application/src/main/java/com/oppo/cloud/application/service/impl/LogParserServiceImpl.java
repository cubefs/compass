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

package com.oppo.cloud.application.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.application.config.CustomConfig;
import com.oppo.cloud.application.config.HadoopConfig;
import com.oppo.cloud.application.config.KafkaConfig;
import com.oppo.cloud.application.constant.RetCode;
import com.oppo.cloud.application.domain.LogPathJoin;
import com.oppo.cloud.application.domain.ParseRet;
import com.oppo.cloud.application.domain.RealtimeTaskInstance;
import com.oppo.cloud.application.domain.Rule;
import com.oppo.cloud.application.producer.MessageProducer;
import com.oppo.cloud.application.service.LogParserService;
import com.oppo.cloud.application.util.EscapePathUtil;
import com.oppo.cloud.application.util.HDFSUtil;
import com.oppo.cloud.application.util.StringUtil;
import com.oppo.cloud.common.domain.cluster.hadoop.NameNodeConf;
import com.oppo.cloud.mapper.TaskApplicationMapper;
import com.oppo.cloud.model.TaskApplication;
import com.oppo.cloud.model.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志解析服务
 */
@Service
@Slf4j
public class LogParserServiceImpl implements LogParserService {

    /**
     * hadoop节点资源
     */
    @Autowired
    private HadoopConfig hadoopConfig;

    @Autowired
    private CustomConfig customConfig;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private MessageProducer messageProducer;

    /**
     * 原生sql查询
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 任务 application表管理
     */
    @Autowired
    private TaskApplicationMapper taskApplicationMapper;
    /**
     * hadoop文件读取节点配置
     */
    private Map<String, NameNodeConf> nameNodeMap;
    /**
     * 任务成功状态
     */
    private static final String TASK_STATE_SUCCESS = "success";

    /**
     * 任务失败状态
     */
    private static final String TASK_STATE_FAIL = "fail";

    /**
     * 任务其他状态
     */
    private static final String TASK_STATE_OTHER = "other";

    /**
     * Flink Task Type
     */
    private static final String TASK_TYPE_FLINK = "FLINK";

    /**
     * Spark Application Id
     */
    private static final String APPLICATION_ID = "applicationId";

    /**
     * 日志地址存储
     */
    private final static String LOG_PATH_KEY = "__log_path";

    /**
     * 获取hadoop集群配置信息
     */
    public synchronized Map<String, NameNodeConf> getNameNodeMap() {
        if (nameNodeMap == null) {
            nameNodeMap = initNameNode();
        }
        return nameNodeMap;
    }

    /**
     * 初始化配置信息
     */
    public Map<String, NameNodeConf> initNameNode() {
        Map<String, NameNodeConf> m = new HashMap<>();
        if (hadoopConfig.getNamenodes() == null) {
            return m;
        }

        for (NameNodeConf nameNodeConf : hadoopConfig.getNamenodes()) {
            m.put(nameNodeConf.getNameservices(), nameNodeConf);
        }
        return m;
    }

    /**
     * filter task instance without matching rule.
     */
    public boolean skipTaskInstance(TaskInstance taskInstance) {
        // 实例状态为空或者非成功、失败状态
        if (StringUtils.isBlank(taskInstance.getTaskState())) {
            return true;
        }
        if (taskInstance.getTaskState().equals(TASK_STATE_OTHER)) {
            if (StringUtils.isBlank(taskInstance.getTaskType())
                    || !taskInstance.getTaskType().equals(TASK_TYPE_FLINK)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 任务处理
     */
    @Override
    public ParseRet handle(TaskInstance taskInstance, Map<String, String> rawData) throws Exception {
        log.debug("收到task instance,{}", taskInstance);
        if (skipTaskInstance(taskInstance)) {
            return new ParseRet(RetCode.RET_SKIP, null);
        }
        // 获取完整的数据
        Map<String, Object> data;
        String sql = null;
        Object[] args = null;
        try {
            if (taskInstance.getId() != null && taskInstance.getId() != 0) {
                sql = "SELECT * FROM task_instance WHERE id = ?";
                args = new Object[]{taskInstance.getId()};
            } else {
                sql = "SELECT * FROM task_instance WHERE flow_name = ? and task_name = ? and execution_time = ?";
                args = new Object[]{taskInstance.getFlowName(), taskInstance.getTaskName(),
                        taskInstance.getExecutionTime()};
            }
            data = jdbcTemplate.queryForMap(sql, args);
        } catch (Exception e) {
            log.error("exception:" + e + ", sql=" + sql + ", args=" + Arrays.toString(args) + ",taskInstance="
                    + taskInstance);
            return new ParseRet(RetCode.RET_EXCEPTION, taskInstance);
        }
        // 补充其他数据依赖
        if (rawData != null) {
            for (Map.Entry<String, String> map : rawData.entrySet()) {
                if (!data.containsKey(map.getKey())) {
                    data.put(map.getKey(), map.getValue());
                }
            }
        }
        // 获取task instance全部信息
        taskInstance = new JSONObject(data).toJavaObject(TaskInstance.class);

        int count = 0;
        List<String> logPathList = new ArrayList<>();
        ParseRet parseRet = new ParseRet(RetCode.RET_SKIP, null);

        for (Rule rule : customConfig.getRules()) {
            LogParser logParser = new LogParser(data, rule, count, taskInstance.getTaskType());
            try {
                RetCode retCode = logParser.extract();
                parseRet = new ParseRet(retCode, taskInstance);
                data = logParser.getData();
                logPathList.add((String) data.get(LOG_PATH_KEY));
                if (retCode != RetCode.RET_OK) {
                    break;
                }
                count++; // 计数
            } catch (Exception e) {
                log.error("parseError:", e);
                parseRet = new ParseRet(RetCode.RET_EXCEPTION, taskInstance);
                break;
            }
        }

        log.debug("parseRet==>" + parseRet);
        // 非成功解析的返回
        if (parseRet.getRetCode() != RetCode.RET_OK && parseRet.getRetCode() != RetCode.RET_DATA_NOT_EXIST) {
            return parseRet;
        }

        String logPath = String.join(",", logPathList);
        // 保存 applicationId
        Object applicationId = data.get(APPLICATION_ID);
        if (applicationId instanceof List) {
            // 去重 applicationId
            Set<String> setId = new HashSet<>();
            for (Object appId : (List) applicationId) {
                if (setId.add((String) appId)) {
                    addTaskApplication((String) appId, taskInstance, logPath);
                }
            }
        } else {
            addTaskApplication((String) applicationId, taskInstance, logPath);
        }
        log.info("project: {}, process:{}, task:{}, execute_time: {}, parse applicationId done!",
                taskInstance.getProjectName(), taskInstance.getFlowName(), taskInstance.getTaskName(),
                taskInstance.getExecutionTime());
        parseRet.setRetCode(RetCode.RET_OK);
        return parseRet;
    }

    /**
     * 添加任务applicationId
     */
    public void addTaskApplication(String applicationId, TaskInstance taskInstance, String logPath) {
        // 数据写回kafka订阅
        log.debug("application save: applicationId=" + applicationId + " task_instance=" + taskInstance + ",lopPath="
                + logPath);

        TaskApplication taskApplication = new TaskApplication();
        taskApplication.setApplicationId(applicationId);
        taskApplication.setProjectName(taskInstance.getProjectName());
        taskApplication.setTaskName(taskInstance.getTaskName());
        taskApplication.setFlowName(taskInstance.getFlowName());
        taskApplication.setExecuteTime(taskInstance.getExecutionTime());
        taskApplication.setRetryTimes(taskInstance.getRetryTimes());
        taskApplication.setLogPath(logPath);
        taskApplication.setTaskType(taskInstance.getTaskType());
        taskApplication.setCreateTime(new Date());
        taskApplication.setUpdateTime(new Date());

        try {
            taskApplicationMapper.insertSelective(taskApplication);
        } catch (DuplicateKeyException e) {
            return;
            // duplicate key with return
        } catch (Exception e) {
            log.error("insertErr:" + e.getMessage());
        }

        try {
            messageProducer.sendMessageSync(kafkaConfig.getTaskApplicationTopic(),
                    JSON.toJSONString(taskApplication));
        } catch (Exception ex) {
            log.error("failed to send insert data to kafka, err: " + ex.getMessage());
        }
    }

    /**
     * 日志解析类
     */
    class LogParser {

        /**
         * 中间数据存储
         */
        private Map<String, Object> data;
        /**
         * 日志解析规则
         */
        private Rule rule;

        /**
         * 解析次序
         */
        private final int index;

        /**
         * 读取hadoop文件延迟时间
         */
        private final int[] SLEEP_TIME = new int[]{20, 40, 60, 80, 100};

        /**
         * task type
         */
        private String taskType = "";

        public LogParser(Map<String, Object> data, Rule rule, int index, String taskType) {
            this.data = data;
            this.rule = rule;
            this.index = index;

            if (taskType != null) {
                this.taskType = taskType;
            }

        }

        /**
         * 日志提取
         */
        public RetCode extract() throws Exception {
            if (!StringUtils.isBlank(rule.getLogPathDep().getQuery())) {
                String sql = StringUtil.replaceParams(rule.getLogPathDep().getQuery(), data);
                log.info("extract SQL:{}, data:{}", sql, data);
                Map<String, Object> depData = null;
                try {
                    depData = jdbcTemplate.queryForMap(sql);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return RetCode.RET_EXCEPTION;
                }

                for (String key : depData.keySet()) {
                    data.put(key, depData.get(key));
                }
            }

            String logPath = this.getLogPath();
            if (StringUtils.isBlank(logPath)) {
                return RetCode.RET_DATA_NOT_EXIST;
            }

            log.info("logPath:{}", logPath);
            NameNodeConf nameNodeConf = HDFSUtil.getNameNode(getNameNodeMap(), logPath);
            if (nameNodeConf == null) {
                log.error("logPath: {} does not have hadoop config", logPath);
                return RetCode.RET_EXCEPTION;
            }
            List<String> filePaths = null;
            try {
                for (int i = 0; i < 10; i++) {
                    filePaths = HDFSUtil.filesPattern(nameNodeConf, String.format("%s*", logPath));
                    // flume文件未上传完成，有.tmp文件
                    if (filePaths.size() != 0 && filePaths.get(filePaths.size() - 1).endsWith(".tmp")) {
                        log.warn("tmp file retry times:{}, {}", i, filePaths.get(filePaths.size() - 1));
                        // 等待完成
                        TimeUnit.SECONDS.sleep(5);
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("filesPattern_error:" + e);
                return RetCode.RET_OP_NEED_RETRY;
            }

            if (filePaths.size() == 0) {
                log.error("logPath: {} does not exist", logPath);
                return RetCode.RET_OP_NEED_RETRY;
            }

            // 记录日志路径
            logPath = String.join(",", filePaths);
            data.put(LOG_PATH_KEY, logPath);

            boolean hasApplicationIds = false;
            int countFileIfHasContent = 0;
            Pattern pattern = Pattern.compile(rule.getExtractLog().getRegex());

            for (String filePath : filePaths) {
                String[] lines = HDFSUtil.readLines(nameNodeConf, filePath);
                log.info(filePath + " has no log content");
                if (lines.length > 0) {
                    countFileIfHasContent += 1;
                }
                // 提取关键字
                for (String line : lines) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String matchVal = matcher.group(rule.getExtractLog().getName());

                        if (this.data.get(rule.getExtractLog().getName()) != null) { // 值已经存在， 原来值变为列表类型
                            Object val = this.data.get(rule.getExtractLog().getName());
                            // 如果是applicationId，可能有多个
                            if (val instanceof List) {
                                ((List) val).add(matchVal);
                            } else {
                                List l = new ArrayList<Object>();
                                l.add(val);
                                l.add(matchVal);
                                val = l;
                            }
                            this.data.put(rule.getExtractLog().getName(), val);
                        } else {
                            this.data.put(rule.getExtractLog().getName(), matchVal);
                        }
                        hasApplicationIds = true;
                    }
                }
            }

            if (hasApplicationIds) {
                return RetCode.RET_OK;
            }

            // 可能没有日志
            if (taskType.equals(TASK_TYPE_FLINK)) {
                log.info("filePaths Count=" + filePaths.size() + ", hasContentCount=" + countFileIfHasContent);
                return RetCode.RET_OP_NEED_RETRY;
            }

            return RetCode.RET_DATA_NOT_EXIST;
        }

        /**
         * 获取日志路径
         */
        public String getLogPath() {
            List<String> paths = new ArrayList<>();
            for (LogPathJoin logPathJoin : rule.getLogPathJoins()) {
                if (StringUtils.isBlank(logPathJoin.getColumn())) {
                    paths.add(logPathJoin.getData());
                } else {
                    log.info("logPathJoin:{}, data:{}", logPathJoin, data);
                    Object columnDataObj = this.data.get(logPathJoin.getColumn());
                    if (columnDataObj == null) {
                        log.error("getColumnData value null, key=" + logPathJoin.getColumn() + ",data=" + this.data);
                        return "";
                    }
                    String columnData = columnDataObj.toString();
                    Pattern pattern = Pattern.compile(logPathJoin.getRegex());
                    Matcher matcher = pattern.matcher(columnData);
                    if (matcher.matches()) {
                        String matchedData = matcher.group(logPathJoin.getName());
                        matchedData = EscapePathUtil.escape(matchedData);
                        if (StringUtils.isNotBlank(logPathJoin.getData())) {
                            paths.add(logPathJoin.getData() + matchedData);
                        } else {
                            paths.add(matchedData);
                        }
                    } else {
                        log.error("`{}` does not match `{}`", logPathJoin.getRegex(), columnData);
                    }
                }
            }

            return String.join("/", paths);
        }

        /**
         * 获取数据
         */
        public Map<String, Object> getData() {
            return this.data;
        }
    }
}
