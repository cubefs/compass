package com.oppo.cloud.diagnosis.consumer;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.diagnosis.domain.dto.RealtimeTaskInstance;
import com.oppo.cloud.diagnosis.service.RealtimeMetaService;
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
    RealtimeMetaService realtimeMetaService;

    /**
     * 日志消费
     */
    @KafkaListener(topics = "${spring.kafka.realtimeTaskTopic}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        Consumer consumer,
                        Acknowledgment ack) {
        log.info(String.format("%d, From partition %d: %s", consumer.hashCode(), partition, message));
        // 解析数据结构
        RealtimeTaskInstance realtimeTaskInstance = JSON.parseObject(message, RealtimeTaskInstance.class);
        realtimeMetaService.saveRealtimeMetaOnYarn(realtimeTaskInstance.getTaskInstance(), realtimeTaskInstance.getApplicationId());
        consumer.commitSync();
    }
}
