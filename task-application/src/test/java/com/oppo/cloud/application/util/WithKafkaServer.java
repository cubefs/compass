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

package com.oppo.cloud.application.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public interface WithKafkaServer {

    String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.5.0";
    KafkaContainer kafkaServer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE_NAME));

    @BeforeAll
    static void startKafkaServer() {
        kafkaServer.start();
    }

    @AfterAll
    static void stopKafkaServer() {
        kafkaServer.stop();
    }
}

