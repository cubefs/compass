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

package com.oppo.cloud.common.util.opensearch;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

/**
 * es客户端配置
 */
public class OpenSearchClient {

    public static final String DEFAULT_SCHEME_NAME = "http";

    public static final String SSL_SCHEME_NAME = "https";

    /**
     * 创建客户连接端
     */
    public static RestHighLevelClient create(HttpHost[] httpHosts, String username, String password, String truststore, String truststorePassword) {
        if (StringUtils.isNotBlank(truststore)) {
            System.setProperty("javax.net.ssl.trustStore", truststore);
            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
        }
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return new RestHighLevelClient(RestClient.builder(httpHosts).setHttpClientConfigCallback(httpAsyncClientBuilder -> {
            httpAsyncClientBuilder.disableAuthCaching();
            return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }));
    }

    /**
     * 创建客户连接端
     */
    public static RestHighLevelClient create(String hosts, String username, String password, String truststore, String truststorePassword) {
        boolean ssl = StringUtils.isNotBlank(truststore);
        return create(buildDefaultHosts(hosts, ssl), username, password, truststore, truststorePassword);
    }

    /**
     * 分隔处理，格式化hosts列表字符串, 如"10.23.232.23:9200,10.23.232.24:9200,10.23.232.25:9200"
     */
    public static HttpHost[] buildHosts(String hosts, String delimiter, boolean ssl) {
        String[] hostList = hosts.split(delimiter);
        HttpHost[] httpHosts = new HttpHost[hostList.length];
        for (int i = 0; i < hostList.length; i++) {
            String[] fields = hostList[i].split(":");
            if (ssl) {
                httpHosts[i] = new HttpHost(fields[0], Integer.parseInt(fields[1]), SSL_SCHEME_NAME);
            } else {
                httpHosts[i] = new HttpHost(fields[0], Integer.parseInt(fields[1]), DEFAULT_SCHEME_NAME);
            }

        }
        return httpHosts;
    }

    /**
     * 默认逗号分割处理主机列表
     */
    public static HttpHost[] buildDefaultHosts(String hosts, boolean ssl) {
        return buildHosts(hosts, ",", ssl);
    }
}
