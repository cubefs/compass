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

package com.oppo.cloud.meta.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 匹配工具类
 */
@Slf4j
public class MatcherUtil {

    /**
     * 获取单个group分组数据
     */
    public static String getGroupData(String content, Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            log.error("matcher not found {} :", name);
            return "";
        }
        String data;
        try {
            data = matcher.group(name);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
        return data;
    }
}
