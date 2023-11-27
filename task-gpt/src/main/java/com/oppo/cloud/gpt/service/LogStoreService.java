package com.oppo.cloud.gpt.service;

/**
 * Log store service
 */
public interface LogStoreService {

    void updateAdvice(String index, String id, String advice);

    void AddToQueue(String message);
}
