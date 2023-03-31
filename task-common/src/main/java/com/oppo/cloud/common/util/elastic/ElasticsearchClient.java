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

package com.oppo.cloud.common.util.elastic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * es客户端配置
 */
public class ElasticsearchClient {

    /**
     * 创建客户连接端
     */
    public static RestHighLevelClient create(HttpHost[] httpHosts, String username, String password) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        return new RestHighLevelClient(RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                    httpAsyncClientBuilder.disableAuthCaching();
                    return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }));
    }

    /**
     * 创建客户连接端
     */
    public static RestHighLevelClient create(String hosts, String username, String password) {
        return create(buildDefaultHosts(hosts), username, password);
    }

    /**
     * 分隔处理，格式化hosts列表字符串, 如"10.23.232.23:9200,10.23.232.24:9200,10.23.232.25:9200"
     */
    public static HttpHost[] buildHosts(String hosts, String delimiter) {
        String[] hostList = hosts.split(delimiter);
        HttpHost[] httpHosts = new HttpHost[hostList.length];
        for (int i = 0; i < hostList.length; i++) {
            String[] fields = hostList[i].split(":");
            httpHosts[i] = new HttpHost(fields[0], Integer.parseInt(fields[1]), "http");
        }
        return httpHosts;
    }

    /**
     * 默认逗号分割处理主机列表
     */
    public static HttpHost[] buildDefaultHosts(String hosts) {
        return buildHosts(hosts, ",");
    }
}
