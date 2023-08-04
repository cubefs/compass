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

import java.text.DecimalFormat;

public class UnitUtil {

    private static final long HOUR = 3600000;

    private static final long DAY = 360000000;

    /**
     * 字节大小动态转换
     */
    public static String transferByte(double v) {
        if (v < 1024) {
            return v + " B";
        }
        int z = (63 - Long.numberOfLeadingZeros((long) v)) / 10;
        return String.format("%.1f %sB", v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    /**
     * 行数动态转换
     */
    public static String transferRows(double data) {
        if (data < Math.pow(10, 4)) {
            return String.format("%.2f", data);
        } else if (data >= Math.pow(10, 4) && data < Math.pow(10, 8)) {
            return String.format("%.2f万", data / Math.pow(10, 4));
        } else {
            return String.format("%.2f亿", data / Math.pow(10, 8));
        }
    }

    /**
     * 资源消耗转换
     */
    public static String transferVcoreS(long data) {
        if (data > 3600) {
            return String.format("%dvcore·h", data / 3600);
        } else {
            return String.format("%dvcore·s", data);
        }
    }

    /**
     * B转GB
     */
    public static double transferBToGB(Long v) {
        return v / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * KB转GB
     */
    public static double transferKBToGB(Long v) {
        return transferDouble(v / (1024.0 * 1024.0));
    }

    /**
     * B转MB
     */
    public static double transferBToMB(Long v) {
        return v / (1024.0 * 1024.0);
    }

    /**
     * MB转GB
     */
    public static double transferMBToGB(Long v) {
        return transferDouble(v / 1024.0);
    }

    /**
     * double保留两位小数，太小的就返回原始值
     */
    public static double transferDouble(double data) {
        double value = (double) Math.round(data * 100) / 100;
        return value == 0 ? (double) Math.round(data * 10000) / 10000 : value;
    }

    /**
     * 将时间戳动态转换为秒、分钟、小时的字符串
     */

    public static String transferSecond(double timestamp) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (timestamp < 60) {
            return decimalFormat.format(timestamp) + "s";
        } else if (timestamp >= 60 && timestamp < 3600) {
            return decimalFormat.format(timestamp / 60) + "m";
        } else if (timestamp >= 3600 && timestamp < (24 * 3600)) {
            return decimalFormat.format(timestamp / 3600) + "h";
        } else {
            return decimalFormat.format(timestamp / (24 * 3600)) + "d";
        }
    }

    /**
     * 耗时字符串动态转化为秒
     */
    public static double transferSecond(String durationStr) {
        String unit = "";
        if (durationStr.contains("s")) {
            unit = "s";
        } else if (durationStr.contains("m")) {
            unit = "m";
        } else if (durationStr.contains("h")) {
            unit = "h";
        } else if (durationStr.contains("d")) {
            unit = "d";
        }
        return Double.parseDouble(durationStr.replace(unit, ""));
    }

    /**
     * 报告总览单位转化
     */
    public static String getTimeUnit(double time) {
        if (time <= HOUR) {
            return "s";
        } else if (time > HOUR && time <= DAY) {
            return "h";
        } else {
            return "d";
        }
    }

    /**
     * vcoreSeconds单位转化
     */
    public static double convertCpuUnit(String timeUnit, double value) {
        switch (timeUnit) {
            case "h":
                return (double) Math.round(value / 3600);
            case "d":
                return (double) Math.round(value / (24 * 3600));
            default:
                return value;
        }
    }

    /**
     * memorySeconds(MB-Seconds)单位转化
     */
    public static double convertMemoryUnit(String timeUnit, double value) {
        double result = 0.0;
        switch (timeUnit) {
            case "s":
                result = (double) Math.round(value / 1024);
                break;
            case "h":
                result = (double) Math.round(value / (1024 * 3600));
                break;
            case "d":
                result = (double) Math.round(value / (1024 * 24 * 3600));
                break;
            default:
                return result;
        }
        return result;
    }

    /**
     * 字符串颜色加红色标签
     */
    public static String transferRed(String keyword) {
        return String.format("<span style=\"color: #e24a4a;\">%s</span>", keyword);
    }

    public static String transferRed(double keyword) {
        return String.format("<span style=\"color: #e24a4a;\">%.2f</span>", keyword);
    }

}
