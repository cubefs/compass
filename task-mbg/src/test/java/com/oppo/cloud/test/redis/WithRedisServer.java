package com.oppo.cloud.test.redis;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public interface WithRedisServer {

    String REDIS_IMAGE_NAME = "redis:7.2.1";
    int REDIS_PORT = 6379;

    GenericContainer<?> redisServer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE_NAME))
            .withExposedPorts(REDIS_PORT);

    @BeforeAll
    static void startRedisServer() {
        redisServer.start();
        System.setProperty("spring.redis.host", redisServer.getHost());
        System.setProperty("spring.redis.port", redisServer.getMappedPort(REDIS_PORT).toString());
    }

    @AfterAll
    static void stopRedisServer() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

}
