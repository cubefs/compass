package com.oppo.cloud.diagnosis.consumer;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.diagnosis.service.FlinkMetaService;
import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskAppExample;
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

import java.util.List;

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

    /**
     * 日志消费task app 元數據
     */
    @KafkaListener(topics = "${spring.kafka.flinkTaskApp}", containerFactory = "kafkaListenerContainerFactory")
    public void receiveFlinkTaskApp(@Payload String message,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         Consumer consumer,
                                         Acknowledgment ack) {
        log.debug(String.format("%d, From partition %d: %s", consumer.hashCode(), partition, message));
        // 解析数据结构
        FlinkTaskApp flinkTaskApp = JSON.parseObject(message, FlinkTaskApp.class);
        try {
            FlinkTaskAppExample flinkTaskAppExample = new FlinkTaskAppExample();
            flinkTaskAppExample.createCriteria()
                    .andApplicationIdEqualTo(flinkTaskApp.getApplicationId());
            List<FlinkTaskApp> flinkTaskApps = flinkTaskAppMapper.selectByExample(flinkTaskAppExample);
            if (flinkTaskApps == null || flinkTaskApps.size() == 0) {
                flinkTaskAppMapper.insert(flinkTaskApp);
            } else if (flinkTaskApps.size() == 1) {
                FlinkTaskApp pre = flinkTaskApps.get(0);
                pre.setTaskState(flinkTaskApp.getTaskState());
                flinkTaskAppMapper.updateByPrimaryKeySelective(pre);
            } else {
                log.error("realtimeTaskApps size 大于1 , appid:{}", flinkTaskApp.getApplicationId());
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        consumer.commitSync();
    }

}
