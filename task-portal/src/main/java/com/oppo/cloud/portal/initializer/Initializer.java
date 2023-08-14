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

package com.oppo.cloud.portal.initializer;

import com.oppo.cloud.common.domain.elasticsearch.*;
import com.oppo.cloud.common.util.elastic.MappingApi;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 初始化服务
 */
@Slf4j
@Component
public class Initializer implements CommandLineRunner {

    @Value(value = "${custom.elasticsearch.appIndex.name}")
    private String appIndex;

    @Value(value = "${custom.elasticsearch.appIndex.shards}")
    private Integer appIndexShards;

    @Value(value = "${custom.elasticsearch.appIndex.replicas}")
    private Integer appIndexReplicas;

    @Value(value = "${custom.elasticsearch.jobIndex.name}")
    private String jobIndex;

    @Value(value = "${custom.elasticsearch.jobIndex.shards}")
    private Integer jobIndexShards;

    @Value(value = "${custom.elasticsearch.jobIndex.replicas}")
    private Integer jobIndexReplicas;

    @Value(value = "${custom.elasticsearch.logIndex.name}")
    private String logIndex;

    @Value(value = "${custom.elasticsearch.logIndex.shards}")
    private Integer logIndexShards;

    @Value(value = "${custom.elasticsearch.logIndex.replicas}")
    private Integer logIndexReplicas;

    @Value(value = "${custom.elasticsearch.jobInstanceIndex.name}")
    private String jobInstanceIndex;

    @Value(value = "${custom.elasticsearch.jobInstanceIndex.shards}")
    private Integer jobInstanceIndexShards;

    @Value(value = "${custom.elasticsearch.jobInstanceIndex.replicas}")
    private Integer jobInstanceIndexReplicas;

    @Value(value = "${custom.elasticsearch.flinkReportIndex.name}")
    private String flinkReportIndex;

    @Value(value = "${custom.elasticsearch.flinkReportIndex.shards}")
    private Integer flinkReportIndexShards;

    @Value(value = "${custom.elasticsearch.flinkReportIndex.replicas}")
    private Integer flinkReportIndexReplicas;

    @Value(value = "${custom.elasticsearch.flinkTaskAnalysisIndex.name}")
    private String flinkTaskAnalysisIndex;

    @Value(value = "${custom.elasticsearch.flinkTaskAnalysisIndex.shards}")
    private Integer flinkTaskAnalysisIndexShards;

    @Value(value = "${custom.elasticsearch.flinkTaskAnalysisIndex.replicas}")
    private Integer flinkTaskAnalysisIndexReplicas;

    @Autowired
    @Qualifier("elasticsearch")
    private RestHighLevelClient client;

    /**
     * 运行初始化服务: 创建模板表
     */
    @Override
    public void run(String... args) throws Exception {
        MappingApi mappingApi = new MappingApi();

        if (!mappingApi.existsTemplate(client, jobIndex)) {
            Map<String, Object> mapping = JobAnalysisMapping.build(true);
            AcknowledgedResponse response = mappingApi.putTemplate(client, jobIndex,
                    new String[]{jobIndex + "-*"}, mapping, jobIndexShards, jobIndexReplicas);
            log.info("Create elasticsearch template {}, result: {}", jobIndex, response.isAcknowledged());
        }

        if (!mappingApi.existsTemplate(client, appIndex)) {
            Map<String, Object> mapping = TaskAppMapping.build(true);
            AcknowledgedResponse response = mappingApi.putTemplate(client, appIndex,
                    new String[]{appIndex + "-*"}, mapping, appIndexShards, appIndexReplicas);
            log.info("Create elasticsearch template {}, result: {}", appIndex, response.isAcknowledged());
        }

        if (!mappingApi.existsTemplate(client, flinkReportIndex)) {
            Map<String, Object> mapping = FlinkReportMapping.build(true);
            AcknowledgedResponse response = mappingApi.putTemplate(client, flinkReportIndex,
                    new String[]{flinkReportIndex + "-*"}, mapping, flinkReportIndexShards, flinkReportIndexReplicas);
            log.info("Create elasticsearch template {}, result: {}", flinkReportIndex, response.isAcknowledged());
        }

        if (!mappingApi.existsTemplate(client, flinkTaskAnalysisIndex)) {
            Map<String, Object> mapping = FlinkTaskAnalysisMapping.build(true);
            AcknowledgedResponse response = mappingApi.putTemplate(client, flinkTaskAnalysisIndex,
                    new String[]{flinkTaskAnalysisIndex + "-*"}, mapping, flinkTaskAnalysisIndexShards, flinkTaskAnalysisIndexReplicas);
            log.info("Create elasticsearch template {}, result: {}", flinkTaskAnalysisIndex, response.isAcknowledged());
        }

        // spark log summary
        if (!mappingApi.existsTemplate(client, logIndex)) {
            Map<String, Object> mapping = LogSummaryMapping.build(true);
            AcknowledgedResponse response = mappingApi.putTemplate(client, logIndex,
                    new String[]{logIndex + "-*"}, mapping, logIndexShards, logIndexReplicas);
            log.info("Create elasticsearch template {}, result: {}", logIndex, response.isAcknowledged());
        }

        if (!mappingApi.existsTemplate(client, jobInstanceIndex)) {
            Map<String, Object> mapping = JobInstanceMapping.build(true);
            AcknowledgedResponse response = mappingApi.putTemplate(client, jobInstanceIndex,
                    new String[]{jobInstanceIndex + "-*"}, mapping, jobInstanceIndexShards, jobInstanceIndexReplicas);
            log.info("Create elasticsearch template {}, result: {}", jobInstanceIndex, response.isAcknowledged());
        }
    }
}
