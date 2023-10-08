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

import com.oppo.cloud.syncer.domain.ValueMapping;
import com.oppo.cloud.syncer.util.databuild.*;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Data conversion tool
 */
@Slf4j
public class DataUtil {

    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Generate map into corresponding objects based on data instances
     */
    public static Object parseInstance(Map<String, String> data, Class clazz) {
        DataFactory<DataBuilder> dataFactory = new GenericFactory<>();
        return dataFactory.getBuilder(clazz).run(data);
    }

    /**
     * String conversion shaping
     */
    public static Integer parseInteger(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Integer.parseInt(s);
    }

    /**
     * Convert string to floating point
     */
    public static Float parseFloat(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Float.parseFloat(s);
    }

    /**
     * date conversion
     */
    public static Date parseDate(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATETIME_FORMAT);
        try {
            return simpleDateFormat.parse(s);
        } catch (Exception e) {
            log.error("failed to parse Date: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Check if the data is empty
     */
    public static Boolean isEmpty(Map<String, String> data) {
        if (data == null || data.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if the data is empty
     */
    public static Boolean isEmpty(List<Map<String, String>> data) {
        if (data == null || data.size() == 0) {
            return true;
        }
        return false;
    }

    public static Boolean isEmpty(String data) {
        if (data == null || data.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Original and target value mappings match
     */
    public static Map<String, String> mapData(Map<String, String> rawData, Map<String, String> columnMapping) {
        Map<String, String> data = new HashMap<>();
        for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
            String key = entry.getKey();
            String mapKey = entry.getValue();
            String value = rawData.getOrDefault(mapKey, null);
            if (value == null) {
                continue; // value does not exist.
            }
            data.put(key, value);
        }
        return data;
    }

    /**
     * Original and target value mappings match
     */
    public static List<Map<String, String>> mapData(List<Map<String, String>> list, Map<String, String> columnMapping) {
        List<Map<String, String>> datas = new ArrayList<>();
        for (Map<String, String> rawData : list) {
            Map<String, String> data = mapData(rawData, columnMapping);
            if (data.size() > 0) {
                datas.add(data);
            }
        }
        return datas;
    }

    /**
     * Field value mapping
     */
    public static List<Map<String, String>> mapColumnValue(List<Map<String, String>> datas,
                                                           Map<String, List<ValueMapping>> columanValueMapping) {
        if (columanValueMapping == null) {
            return datas;
        }

        for (Map.Entry<String, List<ValueMapping>> entry : columanValueMapping.entrySet()) {
            String column = entry.getKey();
            for (Map<String, String> data : datas) {
                String value = data.get(column);
                if (value == null) {
                    continue;
                }
                data.put(column, mapValue(value, entry.getValue()));
            }
        }
        return datas;
    }

    /**
     * Map specific values
     */
    public static String mapValue(String value, List<ValueMapping> valueMappings) {
        for (ValueMapping valueMapping : valueMappings) {
            Map<String, String> m = new HashMap<>();
            for (String v : valueMapping.getOriginValue()) {
                m.put(v, valueMapping.getTargetValue());
            }

            String targetValue = m.get(value);
            if (targetValue != null) {
                return targetValue;
            }
        }
        return value;
    }

    /**
     * Add constant column
     */
    public static List<Map<String, String>> constantColumnValue(List<Map<String, String>> datas,
                                                                Map<String, String> constantColumn) {
        if (constantColumn == null) {
            return datas;
        }

        for (Map<String, String> data : datas) {
            data.putAll(constantColumn);
        }

        return datas;
    }

    public static Object formatDateObject(Object obj) {
        return ((LocalDateTime) obj).format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));

    }
}
