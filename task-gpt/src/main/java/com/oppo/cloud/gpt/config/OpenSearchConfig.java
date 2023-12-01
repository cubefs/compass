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

package com.oppo.cloud.gpt.config;

import com.oppo.cloud.common.util.opensearch.OpenSearchClient;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenSearch Client Configuration.
 */
@Configuration
@Slf4j
public class OpenSearchConfig {
    @Value("${spring.opensearch.nodes}")
    private String nodes;
    @Value("${spring.opensearch.username}")
    private String username;
    @Value("${spring.opensearch.password}")
    private String password;
    @Value("${spring.opensearch.truststore}")
    private String truststore;
    @Value("${spring.opensearch.truststore-password}")
    private String truststorePassword;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return OpenSearchClient.create(nodes, username, password, truststore, truststorePassword);
    }
}
