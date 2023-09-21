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

