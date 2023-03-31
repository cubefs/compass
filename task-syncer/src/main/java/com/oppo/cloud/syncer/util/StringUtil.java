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

package com.oppo.cloud.syncer.util;

import java.util.Map;

/**
 * 字符串处理工具
 */
public class StringUtil {

    /**
     * 替换模板参数
     */
    public static String replaceParams(String template, Map<String, String> params) {
        for (String key : params.keySet()) {
            template = template.replace("${" + key + "}", "'" + params.get(key) + "'");
        }
        return template;
    }

    /**
     * 重写查询
     */
    public static String rewriteQuery(String query) {
        return null;
    }
}
