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

package com.oppo.cloud.common.util;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.cluster.hadoop.YarnConf;
import com.oppo.cloud.common.domain.cluster.yarn.ClusterInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yarn工具类
 */
@Slf4j
public class YarnUtil {

    private static final String API_YARN_CLUSTER_INFO = "http://%s/ws/v1/cluster/info";

    /**
     * 获取Yarn RM列表信息
     *
     * @param yarnConfs
     * @return
     */
    public static Map<String, String> getYarnClusters(List<YarnConf> yarnConfs) {
        log.info("yarn conf: {}", yarnConfs);
        Map<String, String> clusters = new HashMap<>();
        for (YarnConf yarnConf : yarnConfs) {
            String activeHost = getRmActiveHost(yarnConf.getResourceManager());
            if (StringUtils.isBlank(activeHost)) {
                continue;
            }
            clusters.put(activeHost, yarnConf.getClusterName());
        }
        return clusters;
    }

    /**
     * 获取Yarn Active节点
     *
     * @param hosts
     * @return
     */
    public static String getRmActiveHost(List<String> hosts) {
        for (String host : hosts) {
            String yarnApi = String.format(API_YARN_CLUSTER_INFO, host);

            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(yarnApi);
            HttpResponse response = null;
            try {
                response = httpClient.execute(httpGet);
            } catch (Exception e) {
                log.error("failed to get active rm host, err: {}, api: {}", e.getMessage(), yarnApi);
                continue;
            }

            HttpEntity httpEntity = response.getEntity();
            if (httpEntity == null) {
                log.error("get active host, result: null, api: {}", yarnApi);
                continue;
            }

            ClusterInfo clusterInfo = null;
            try {
                clusterInfo = JSON.parseObject(EntityUtils.toString(httpEntity, "UTF-8"), ClusterInfo.class);
            } catch (Exception e) {
                log.error("failed to decode rm info, err: {}, api: {}", e.getMessage(), yarnApi);
                continue;
            }

            if (clusterInfo == null) {
                log.error("get active host, clusterInfo is null, api: {}", yarnApi);
                continue;
            }

            log.info("YarnRmInfo ==> {} : {}", host, clusterInfo.getClusterInfo().getHaState());
            if ("ACTIVE".equals(clusterInfo.getClusterInfo().getHaState())) {
                return host;
            }
        }
        return null;
    }
}
