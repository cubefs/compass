package com.oppo.cloud.gpt.service.impl;

import com.oppo.cloud.gpt.service.LogMatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogMatchServiceImpl implements LogMatchService {

    @KafkaListener(
            topics = "${spring.kafka.consumer.topics}",
            groupId = "${spring.kafka.consumer.group-id}",
            autoStartup = "${spring.kafka.consumer.auto.start}")
    public void matchLog(@Payload List<String> messages, Acknowledgment ack) {
        doMatch(messages);
        ack.acknowledge();
    }

    private void doMatch(final List<String> messages) {
        for (String message : messages) {
            
        }
    }
}
