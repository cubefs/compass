package com.oppo.cloud.gpt.service.impl;


import com.oppo.cloud.gpt.service.LogStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LogStoreServiceImpl implements LogStoreService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.aggregate-consumer.topics}")
    private String topics;

    /**
     * Update advice
     *
     * @param index
     * @param id
     * @param advice
     */
    @Override
    public void updateAdvice(String index, String id, String advice) {
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("advice", advice);

            UpdateRequest request = new UpdateRequest(index, id).doc(doc);
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("failed to update advice, id:{}, index:{}, advice:{}, err:{}", id, index, advice, e.getMessage());
        }
    }

    @Override
    public void AddToQueue(String message) {
        kafkaTemplate.send(topics, message);
    }
}
