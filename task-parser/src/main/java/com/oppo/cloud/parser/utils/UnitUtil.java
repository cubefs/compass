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

package com.oppo.cloud.parser.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit conversion util
 **/

@Slf4j
public class UnitUtil {

    public static Pattern UnitRegex = Pattern.compile("(?<num>[0-9]+)(?<unit>[a-zA-Z]+)");

    /**
     * Other units convert to Byte
     */
    public static Long toBytes(String s) {
        if (StringUtils.isEmpty(s)) {
            return 0L;
        }
        if (StringUtils.isNumeric(s)) {
            log.warn("isNumeric:{}", s);
            return Long.parseLong(s) * 1024 * 1024;
        }
        Matcher matcher = UnitUtil.UnitRegex.matcher(s);
        if (!matcher.matches()) {
            log.error("Can not match unit, raw: {}", s);
            return 0L;
        }
        long num = Long.parseLong(matcher.group("num"));
        String unit = matcher.group("unit");

        switch (unit) {
            case "g":
            case "G":
            case "GB":
                return num * 1024 * 1024 * 1024;
            case "M":
            case "m":
            case "MiB":
                return num * 1024 * 1024;
            default:
                log.error("Unknown unit: {}, raw: {}", unit, s);
                return 0L;
        }
    }

    public static Long MBToByte(Long mb) {
        return mb * 1024 * 1024;
    }

}
