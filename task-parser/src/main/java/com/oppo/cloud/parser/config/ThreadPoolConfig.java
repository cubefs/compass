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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executor pool config
 */
@Configuration
public class ThreadPoolConfig {

    public static final String JOB_THREAD_POOL = "jobThreadPool";

    public static final String TASK_THREAD_POOL = "taskThreadPool";

    public static final String DETECTOR_THREAD_POOL = "detectorThreadPool";

    public static final String PARSER_THREAD_POOL = "parserThreadPool";

    public static final String REDIS_CONSUMER_THREAD_POOL = "redisConsumerThreadPool";

    @Resource
    private CustomConfig config;

    @Bean(name = JOB_THREAD_POOL)
    public Executor jobThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getMaxThreadPoolSize());
        executor.setMaxPoolSize(config.getMaxThreadPoolSize());
        executor.setKeepAliveSeconds(120);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("job-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean(name = TASK_THREAD_POOL)
    public Executor taskThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getMaxThreadPoolSize());
        executor.setMaxPoolSize(config.getMaxThreadPoolSize());
        executor.setKeepAliveSeconds(120);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("task-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean(name = PARSER_THREAD_POOL)
    public Executor parserThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getMaxThreadPoolSize());
        executor.setMaxPoolSize(config.getMaxThreadPoolSize());
        executor.setKeepAliveSeconds(120);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("parser-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean(name = DETECTOR_THREAD_POOL)
    public Executor detectorThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setKeepAliveSeconds(120);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("detector-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }


    @Bean(name = REDIS_CONSUMER_THREAD_POOL)
    public Executor redisConsumerThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getMaxThreadPoolSize());
        executor.setMaxPoolSize(config.getMaxThreadPoolSize());
        executor.setKeepAliveSeconds(120);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("redis-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
