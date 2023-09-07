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

package com.oppo.cloud.flink.config;


import com.oppo.cloud.common.util.opensearch.OpenSearchClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * OpenSearch Config
 */
@Configuration
@Slf4j
@Data
public class OpenSearchFlinkConfig {
    /**
     * 主机列表
     */
    @Value("${spring.opensearch.nodes}")
    private String hosts;
    /**
     * 用户名
     */
    @Value("${spring.opensearch.username}")
    private String username;
    /**
     * 密码
     */
    @Value("${spring.opensearch.password}")
    private String password;

    @Value("${spring.opensearch.truststore}")
    private String truststore;

    @Value("${spring.opensearch.truststore-password}")
    private String truststorePassword;
    /**
     * bean名称
     */
    public final static String SEARCH_CLIENT = "flinkSearchClient";

    @Bean(SEARCH_CLIENT)
    public RestHighLevelClient flinkSearchClient() {
        return OpenSearchClient.create(hosts, username, password, truststore, truststorePassword);
    }

}
