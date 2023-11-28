package com.oppo.cloud.gpt.util;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.*;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatGPTClient {

    private List<String> apiKeys;

    private String proxy;

    private String model;

    public ChatGPTClient(List<String> apiKeys, String proxy, String model) {
        this.apiKeys = apiKeys;
        this.proxy = proxy;
        this.model = model;
    }

    public String completions(String prompt, String text) {
        OpenAiClient client = this.newClient();
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder().role(Message.Role.SYSTEM).content(prompt).build());
        messages.add(Message.builder().role(Message.Role.USER).content(text).build());

        ChatCompletion chatCompletion = ChatCompletion
                .builder()
                .messages(messages)
                .model(this.model) // com.unfbx.chatgpt.entity.chat.BaseChatCompletion.class
                .build();

        ChatCompletionResponse response = client.chatCompletion(chatCompletion);
        if (response.getChoices().isEmpty()) {
            return "";
        }
        System.out.println(response);
        return response.getChoices().get(0).getMessage().getContent();
    }

    private OpenAiClient newClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new OpenAiResponseInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        return OpenAiClient.builder()
                .apiKey(apiKeys)
                .keyStrategy(new KeyRandomStrategy())
                .okHttpClient(okHttpClient)
                .apiHost(proxy)
                .build();
    }
}
