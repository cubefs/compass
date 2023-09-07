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

package com.oppo.cloud.common.domain.opensearch;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OpenSearch 字段类型
 */
public class Mapping {

    /**
     * 创建数字类型字段, t参数: long, integer, double, float
     */
    public static Map<String, Object> digit(String t) {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("type", t))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 创建date类型字段
     */
    public static Map<String, Object> date() {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("type", "date"),
                        new AbstractMap.SimpleEntry<>("format", "yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss.SSS||strict_date_optional_time||epoch_millis"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 创建text,keyword类型字段
     */
    public static Map<String, Object> text() {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("type", "text"),
                        new AbstractMap.SimpleEntry<>("fields", fields()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 创建users复合类型字段
     */
    public static Map<String, Object> users() {
        Map<String, Object> taskUsersProperties = new HashMap<>();
        taskUsersProperties.put("userId", digit("integer"));
        taskUsersProperties.put("username", text());
        return Stream.of(new AbstractMap.SimpleEntry<>("properties", taskUsersProperties))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 创建fields字段
     */
    public static Map<String, Object> fields() {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("keyword", keyword()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 创建keyword字段
     */
    public static Map<String, Object> keyword() {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("type", "keyword"),
                        new AbstractMap.SimpleEntry<>("ignore_above", 512))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 返回 {"enabled": true or false}
     */
    public static Map<String, Object> enabled(boolean t) {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("enabled", t))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 创建object类型
     */
    public static Map<String, Object> object() {
        return Stream.of(
                        new AbstractMap.SimpleEntry<>("type", "object"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
