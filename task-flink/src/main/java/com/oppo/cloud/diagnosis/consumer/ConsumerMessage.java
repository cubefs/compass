package com.oppo.cloud.diagnosis.consumer;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.diagnosis.service.FlinkMetaService;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.TaskApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 消费者
 */
@Slf4j
@Component
public class ConsumerMessage {

    @Autowired
    FlinkMetaService flinkMetaService;

    @Autowired
    FlinkTaskAppMapper flinkTaskAppMapper;
    /**
     * 日志消费task app 元數據
     */
    @KafkaListener(topics = "${spring.kafka.taskApplicationTopic}", containerFactory = "kafkaListenerContainerFactory")
    public void receiveDsTaskApplication(@Payload String message,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         Consumer consumer,
                                         Acknowledgment ack) {
        log.debug(String.format("%d, From partition %d: %s", consumer.hashCode(), partition, message));
        // 解析数据结构
        TaskApplication realtimeTaskInstance = JSON.parseObject(message, TaskApplication.class);
        flinkMetaService.saveRealtimeMetaOnYarn(realtimeTaskInstance);
        consumer.commitSync();
    }

}
