package com.oppo.cloud.diagnosis.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.domain.cluster.yarn.ClusterInfo;
import com.oppo.cloud.common.service.RedisService;
import com.oppo.cloud.diagnosis.config.ClusterConfig;
import com.oppo.cloud.diagnosis.domain.Constant;
import com.oppo.cloud.diagnosis.domain.dto.YarnClusterInfo;
import com.oppo.cloud.diagnosis.service.IClusterMetaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ClusterConfig config;

    @Resource(name = "restTemplate")
    private RestTemplate restTemplate;

    private static final String YARN_CLUSTER_INFO = "http://%s:%s/ws/v1/cluster/info";

    private static final String YARN_CONF = "http://%s:%s/conf";


    private final Pattern yarnPattern = Pattern.compile("^.*\"(?<yarn>.+yarn)\".*$");

    Pattern defaultFSPattern = Pattern.compile(".*<name>fs.defaultFS</name><value>(?<defaultFS>.*?)</value>.*",
            Pattern.DOTALL);

    Pattern remoteDirPattern = Pattern.compile(".*<name>yarn.nodemanager.remote-app-log-dir</name><value>" +
            "(?<remoteDir>.*?)</value>.*", Pattern.DOTALL);


    /**
     * 获取yarn rm列表
     */
    @Override
    public Map<String, String> getYarnClusters() {
        List<YarnClusterInfo> list = config.getYarn();
        if (list == null) {
            list = (List<YarnClusterInfo>) redisService.get(Constant.YARN_CLUSTERS);
            log.info("getYarnClusters from redis:{}", list);
        } else {
            log.info("getYarnClusters from config:{}", list);
        }
        Map<String, String> ips = new HashMap<>();
        for (YarnClusterInfo clusterInfo : list) {
            String activeHost = getRmActiveHost(clusterInfo.getResourceManager());
            if (StringUtils.isEmpty(activeHost)) {
                continue;
            }
            ips.put(activeHost, clusterInfo.getClusterName());
        }
        return ips;
    }


    /**
     * 获取ACTIVE节点
     */
    public String getRmActiveHost(List<String> list) {
        for (String addr : list) {
            String clusterInfoUrl = String.format(YARN_CLUSTER_INFO, addr, config.getRmPort());
            log.info(clusterInfoUrl);
            ResponseEntity<String> responseEntity = null;
            try {
                responseEntity = restTemplate.getForEntity(clusterInfoUrl, String.class);
            } catch (RestClientException e) {
                log.error(e.getMessage());
                continue;
            }
            if (responseEntity == null || responseEntity.getBody() == null) {
                log.error("getRmActiveHost:{}", clusterInfoUrl);
                continue;
            }
            ClusterInfo clusterInfo;
            try {
                clusterInfo = objectMapper.readValue(responseEntity.getBody(), ClusterInfo.class);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                continue;
            }
            if (clusterInfo == null) {
                log.error("{},clusterInfo null", addr);
                continue;
            }
            log.info("{}", clusterInfo);
            log.info("YarnRmInfo-->{}:{}", addr, clusterInfo.getClusterInfo().getHaState());
            if ("ACTIVE".equals(clusterInfo.getClusterInfo().getHaState())) {
                return addr;
            }
        }
        return null;
    }


}
