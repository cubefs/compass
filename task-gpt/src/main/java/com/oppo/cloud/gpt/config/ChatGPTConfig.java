package com.oppo.cloud.gpt.config;

import com.oppo.cloud.gpt.util.ChatGPTClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ChatGPT Configuration
 */

@Data
@Configuration
public class ChatGPTConfig {

    @Value("${chatgpt.enable}")
    private boolean enable;

    @Value("#{'${chatgpt.apiKeys}'.split(',')}")
    private List<String> apiKeys;

    @Value("${chatgpt.proxy}")
    private String proxy;

    @Value("${chatgpt.model}")
    private String model;

    @Value("${chatgpt.prompt}")
    private String prompt;

    @Bean
    public ChatGPTClient client() {
        return new ChatGPTClient(apiKeys, proxy, model);
    }
}
