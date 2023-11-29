package com.oppo.cloud.gpt.service.impl;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.LogMessage;
import com.oppo.cloud.gpt.config.ChatGPTConfig;
import com.oppo.cloud.gpt.drain.IdGenerator;
import com.oppo.cloud.gpt.drain.LogCluster;
import com.oppo.cloud.gpt.service.LogAggregateService;
import com.oppo.cloud.gpt.service.LogStoreService;
import com.oppo.cloud.gpt.service.TemplateService;
import com.oppo.cloud.gpt.util.ChatGPTClient;
import com.oppo.cloud.gpt.util.DataUtil;
import com.oppo.cloud.gpt.util.UUIDIdGenerator;
import com.oppo.cloud.model.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Log Aggregation Implementation
 */
@Slf4j
@Service
public class LogAggregateServiceImpl implements LogAggregateService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private LogStoreService logStoreService;

    @Autowired
    private ChatGPTClient chatGPTClient;

    @Autowired
    private ChatGPTConfig chatGPTConfig;

    private IdGenerator idGen = new UUIDIdGenerator();

    @KafkaListener(
            topics = "${spring.kafka.aggregate-consumer.topics}",
            groupId = "${spring.kafka.aggregate-consumer.group-id}")
    public void aggregateLog(@Payload List<String> messages, Acknowledgment ack) {
        for (String message : messages) {
            doAggregate(message);
        }
    }

    public void doAggregate(String message) {
        LogMessage logMessage = DataUtil.decode(message);
        if (logMessage == null) return;

        String advice = null;
        LogCluster logCluster = this.templateService.aggregate(logMessage.getRawLog());
        switch (logCluster.getState()) {
            case CLUSTER_CREATED: // create new cluster
                String cid = idGen.next();
                logCluster.setId(cid);
                if (chatGPTConfig.isEnable()) {
                    advice = getGptAdvice(logCluster.getTemplate());
                    if (StringUtils.isBlank(advice)) {
                        return; // TODO: send it back to queue
                    }
                }
                templateService.save(genTemplate(cid, logCluster.getTemplate(), advice, logMessage.getRawLog()));
                templateService.addTemplate(logCluster, advice);
                logStoreService.updateAdvice(logMessage.getIndex(), logMessage.getId(), advice);
                break;
            case CLUSTER_CHANGED: // merge cluster
                Template template = templateService.find(logCluster.getId());
                if (template == null) return;

                String[] tokens = templateService.mergeTemplates(
                        template.getCluster().split(" "), logCluster.getTokens());
                logCluster.setTokens(tokens);

                if (StringUtils.isBlank(template.getAdvice()) && chatGPTConfig.isEnable()) {
                    advice = getGptAdvice(logCluster.getTemplate());
                    template.setAdvice(advice);
                }
                templateService.update(logCluster.getId(), logCluster.getTemplate(), template.getAdvice());
                templateService.addTemplate(logCluster, template.getAdvice());
                break;
            case NONE:
                // do nothing
                log.debug("cluster exist, cluster: {}, message: {}", JSON.toJSONString(logCluster), message);
        }
    }

    public Template genTemplate(String cid, String cluster, String advice, String rawLog) {
        Template template = new Template();
        template.setCid(cid);
        template.setCluster(cluster);
        template.setAdvice(advice);
        template.setRawLog(rawLog);
        template.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        template.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return template;
    }

    /**
     * Get solution from chatgpt
     *
     * @param message
     * @return
     */
    private String getGptAdvice(String message) {
        try {
            for (int retry = 0; retry < 3; retry++) {
                String solution = chatGPTClient.completions(chatGPTConfig.getPrompt(), message);
                if (StringUtils.isNotBlank(solution)) {
                    return solution;
                }
                TimeUnit.SECONDS.sleep(10);
            }
        } catch (Exception e) {
            log.error("failed to get gpt solution, message: {}, exception: {}", message, e.getMessage());
        }
        return null;
    }
}
