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
