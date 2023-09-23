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

package com.oppo.cloud.application.config;

import lombok.Data;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * kafka配置
 */
@Configuration
@EnableKafka
@Data
public class KafkaConfig {

    /**
     * 消费主题
     */
    @Value("${spring.kafka.topics}")
    private String topics;
    /**
     * 消费主题
     */
    @Value("${spring.kafka.taskApplicationTopic}")
    private String taskApplicationTopic;
    /**
     * 消费组
     */
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    /**
     * 消费模式: lastest, earliest
     */
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;
    /**
     * kafka broker集群地址
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    /**
     * 两次消费最大间隔时间
     */
    @Value("${spring.kafka.consumer.max-poll-interval-ms}")
    private String maxPollIntervalMs;

    /**
     * 创建消费者
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig(), new StringDeserializer(), new StringDeserializer());
    }

    /**
     * 消费者配置
     */
    public Map<String, Object> consumerConfig() {
        return Stream.of(
                new AbstractMap.SimpleEntry<>(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId()),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.GROUP_ID_CONFIG, groupId),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                        RoundRobinAssignor.class.getName()),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class),
                new AbstractMap.SimpleEntry<>(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 配置listener
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    /**
     * 获取clientId: 表现为kafka memberId前缀
     */
    @Bean(name = "consumerId")
    public String consumerClientId() {
        return String.format("client-%d-%d", Thread.currentThread().getId(), new Random().nextInt());
    }

    /**
     * 获取主题
     */
    @Bean(name = "topics")
    public String getTopics() {
        return this.topics;
    }

    /**
     * 获取brokers
     */
    @Bean(name = "bootstrapServers")
    public String getBootstrapServers() {
        return this.bootstrapServers;
    }

    /**
     * 获取groupId
     */
    @Bean(name = "groupId")
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * 使用Kafka Admin Client API操作
     */
    @Bean(name = "kafkaAdminClient")
    public AdminClient kafkaAdminClient() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(properties);
    }
}
