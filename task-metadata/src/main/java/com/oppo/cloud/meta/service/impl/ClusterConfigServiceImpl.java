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

package com.oppo.cloud.meta.service.impl;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.hadoop.YarnConf;
import com.oppo.cloud.common.domain.cluster.yarn.ClusterInfo;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.meta.config.HadoopConfig;
import com.oppo.cloud.meta.domain.Properties;
import com.oppo.cloud.meta.domain.YarnConfProperties;
import com.oppo.cloud.meta.service.IClusterConfigService;
import com.oppo.cloud.meta.utils.MatcherUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * YARN、SPARK集群地址配置信息
 */
@Slf4j
@Service
public class ClusterConfigServiceImpl implements IClusterConfigService {

    @Resource
    private RedisService redisService;

    @Resource
    private HadoopConfig config;

    @Resource(name = "restTemplate")
    private RestTemplate restTemplate;

    private static final String YARN_CLUSTER_INFO = "http://%s/ws/v1/cluster/info";

    private static final String YARN_CONF = "http://%s/conf";

    private Pattern defaultFSPattern = Pattern.compile(".*<name>fs.defaultFS</name><value>(?<defaultFS>.*?)</value>.*",
            Pattern.DOTALL);

    private Pattern remoteDirPattern = Pattern.compile(".*<name>yarn.nodemanager.remote-app-log-dir</name><value>" +
            "(?<remoteDir>.*?)</value>.*", Pattern.DOTALL);

    /**
     * 获取spark history server列表
     */
    @Override
    public List<String> getSparkHistoryServers() {
        return config.getSpark().getSparkHistoryServer();
    }

    /**
     * 获取yarn rm列表
     */
    @Override
    public Map<String, String> getYarnClusters() {
        List<YarnConf> yarnConfList = config.getYarn();
        Map<String, String> yarnClusters = new HashMap<>();
        for (YarnConf yarnConf : yarnConfList) {
            String activeHost = getRmActiveHost(yarnConf.getResourceManager());
            if (StringUtils.isEmpty(activeHost)) {
                continue;
            }
            yarnClusters.put(activeHost, yarnConf.getClusterName());
        }
        return yarnClusters;
    }

    public String getRmActiveHost(List<String> list) {
        for (String host : list) {
            String clusterInfoUrl = String.format(YARN_CLUSTER_INFO, host);
            ResponseEntity<String> responseEntity;
            try {
                responseEntity = restTemplate.getForEntity(clusterInfoUrl, String.class);
            } catch (Exception e) {
                log.error("Exception:", e);
                continue;
            }
            if (responseEntity.getBody() == null) {
                log.error("get active host null:{}", clusterInfoUrl);
                continue;
            }
            ClusterInfo clusterInfo;
            try {
                clusterInfo = JSON.parseObject(responseEntity.getBody(), ClusterInfo.class);
            } catch (Exception e) {
                log.error("Exception:", e);
                continue;
            }
            if (clusterInfo == null) {
                log.error("get active host null:{}", clusterInfoUrl);
                continue;
            }
            log.info("YarnRmInfo-->{}:{}", host, clusterInfo.getClusterInfo().getHaState());
            if ("ACTIVE".equals(clusterInfo.getClusterInfo().getHaState())) {
                return host;
            }
        }
        return null;
    }

    /**
     * 更新集群信息
     */
    @Override
    public void updateClusterConfig() {

        log.info("clusterConfig:{}", config);
        // cache spark history server
        List<String> sparkHistoryServerList = config.getSpark().getSparkHistoryServer();
        log.info("{}:{}", Constant.SPARK_HISTORY_SERVERS, sparkHistoryServerList);
        redisService.set(Constant.SPARK_HISTORY_SERVERS, JSON.toJSONString(sparkHistoryServerList));

        // cache yarn server
        List<YarnConf> yarnConfList = config.getYarn();
        // resourceManager 对应的 jobHistoryServer
        Map<String, String> rmJhsMap = new HashMap<>();
        yarnConfList.forEach(clusterInfo -> clusterInfo.getResourceManager()
                .forEach(rm -> rmJhsMap.put(rm, clusterInfo.getJobHistoryServer())));
        redisService.set(Constant.YARN_CLUSTERS, JSON.toJSONString(sparkHistoryServerList));
        log.info("{}:{}", Constant.YARN_CLUSTERS, yarnConfList);
        redisService.set(Constant.RM_JHS_MAP, JSON.toJSONString(rmJhsMap));
        log.info("{}:{}", Constant.RM_JHS_MAP, rmJhsMap);
        updateJHSConfig(yarnConfList);
    }

    /**
     * 更新配置中jobhistoryserver hdfs路径信息
     */
    public void updateJHSConfig(List<YarnConf> list) {
        for (YarnConf yarnClusterInfo : list) {
            String host = yarnClusterInfo.getJobHistoryServer();
            String hdfsPath = getHDFSPath(host);
            if (StringUtils.isEmpty(hdfsPath)) {
                log.error("get {}, hdfsPath empty", host);
                continue;
            }
            String key = Constant.JHS_HDFS_PATH + host;
            log.info("cache hdfsPath:{},{}", key, hdfsPath);
            redisService.set(key, hdfsPath);
        }
    }

    /**
     * 获取jobhistoryserver hdfs路径信息
     */
    public String getHDFSPath(String ip) {
        String url = String.format(YARN_CONF, ip);
        log.info("getHDFSPath:{}", url);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
            log.error("getHDFSPathErr:{},{}", url, e.getMessage());
            return null;
        }
        if (responseEntity.getBody() == null) {
            log.error("getHDFSPathErr:{}", url);
            return null;
        }
        YarnConfProperties yarnConfProperties = null;
        try {
            yarnConfProperties = JSON.parseObject(responseEntity.getBody(), YarnConfProperties.class);
        } catch (Exception e) {
            log.error("Exception:", e);
        }

        String remoteDir = "";
        String defaultFS = "";
        if (yarnConfProperties != null && yarnConfProperties.getProperties() != null) {
            for (Properties properties : yarnConfProperties.getProperties()) {
                String key = properties.getKey();
                String value = properties.getValue();
                if ("yarn.nodemanager.remote-app-log-dir".equals(key)) {
                    log.info("yarnConfProperties key: yarn.nodemanager.remote-app-log-dir, value: {}", value);
                    remoteDir = value;
                }
                if ("fs.defaultFS".equals(key)) {
                    log.info("yarnConfProperties key: fs.defaultFS, value: {}", value);
                    defaultFS = value;
                }
            }
        } else {
            remoteDir = MatcherUtil.getGroupData(responseEntity.getBody(), remoteDirPattern, "remoteDir");
            defaultFS = MatcherUtil.getGroupData(responseEntity.getBody(), defaultFSPattern, "defaultFS");
        }

        if (StringUtils.isEmpty(remoteDir)) {
            log.error("remoteDirEmpty:{}", url);
            return null;
        }
        if (!remoteDir.contains("hdfs")) {
            if (StringUtils.isEmpty(defaultFS)) {
                log.error("defaultFSEmpty:{}", url);
                return null;
            }
            remoteDir = defaultFS + remoteDir;
        }
        log.info("getHDFSPath url: {},remoteDir: {}", url, remoteDir);
        return remoteDir;
    }


}
