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
import com.oppo.cloud.common.domain.cluster.yarn.ClusterInfo;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.flink.service.IClusterMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 更新、获取YANR、SPARK集群信息
 */
@Slf4j
@Service
public class ClusterMetaServiceImpl implements IClusterMetaService {
    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;


    @Resource(name = "flinkRestTemplate")
    private RestTemplate restTemplate;

    private static final String YARN_CLUSTER_INFO = "http://%s/ws/v1/cluster/info";

    private static final String YARN_CONF = "http://%s:%s/conf";


    private final Pattern yarnPattern = Pattern.compile("^.*\"(?<yarn>.+yarn)\".*$");

    Pattern defaultFSPattern = Pattern.compile(".*<name>fs.defaultFS</name><value>(?<defaultFS>.*?)</value>.*",
            Pattern.DOTALL);

    Pattern remoteDirPattern = Pattern.compile(".*<name>yarn.nodemanager.remote-app-log-dir</name><value>" +
            "(?<remoteDir>.*?)</value>.*", Pattern.DOTALL);


    /**
     * 获取ACTIVE节点
     */
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


}
