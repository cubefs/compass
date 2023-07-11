/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
