package com.oppo.cloud.diagnosis.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * http util
 */
@Slf4j
public class HttpUtil {
    public String get(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(1, TimeUnit.SECONDS)
//                .readTimeout(1, TimeUnit.SECONDS)
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
