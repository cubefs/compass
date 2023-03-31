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

package com.oppo.cloud.parser.config;

import com.oppo.cloud.common.util.elastic.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * es client
 */
@Configuration
@Slf4j
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.nodes}")
    private String nodes;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    public final static String ELASTIC_CLIENT = "elasticClient";

    @Primary
    @Bean(ELASTIC_CLIENT)
    public RestHighLevelClient restHighLevelClient() {
        return ElasticsearchClient.create(nodes, username, password);
    }

}
