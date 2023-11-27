package com.oppo.cloud.gpt.service.impl;

import com.oppo.cloud.common.domain.LogMessage;
import com.oppo.cloud.gpt.drain.LogCluster;
import com.oppo.cloud.gpt.service.LogMatchService;
import com.oppo.cloud.gpt.service.LogStoreService;
import com.oppo.cloud.gpt.service.TemplateService;
import com.oppo.cloud.gpt.util.DataUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogMatchServiceImpl implements LogMatchService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private LogStoreService logStoreService;

    @KafkaListener(
            topics = "${spring.kafka.match-consumer.topics}",
            groupId = "${spring.kafka.match-consumer.group-id}",
            autoStartup = "${spring.kafka.match-consumer.auto.start}")
    public void matchLog(@Payload List<String> messages, Acknowledgment ack) {
        doMatch(messages);
        ack.acknowledge();
    }

    private void doMatch(final List<String> messages) {
        for (String message : messages) {
            LogMessage logMessage = DataUtil.decode(message);
            if (logMessage == null || StringUtils.isBlank(logMessage.getRawLog())) continue;
            // todo: filter date of the log
            LogCluster logCluster = templateService.match(logMessage.getRawLog(), "never");
            if (logCluster == null) {
                logStoreService.AddToQueue(message);
            } else {
                String advice = templateService.getAdvice(logCluster.getId());
                logStoreService.updateAdvice(logMessage.getIndex(), logCluster.getId(), advice);
                templateService.update(logCluster.getId(), null); // update time
            }
        }
    }
}
