package com.oppo.cloud.portal.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * http util
 */
@Slf4j
@Component
public class HttpUtil {
    public String get(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    public String post(String url, Map<String,Object> body){
        String bodyJson = JSON.toJSONString(body);
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody okBody = RequestBody.create(
                MediaType.parse("application/json"),bodyJson
        );
        final Request request = new Request.Builder()
                .url(url)
                .post(okBody)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
