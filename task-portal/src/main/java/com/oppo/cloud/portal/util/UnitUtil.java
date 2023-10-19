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
import java.util.ArrayList;
import java.util.List;

public class UnitUtil {

    private static final long MilliSecond = 1;
    private static final long Second = 1000 * MilliSecond;
    private static final long Minute = 60 * Second;
    private static final long Hour = 60 * Minute;
    private static final long Day = 24 * Hour;
    private static final long HOUR = 3600000;

    private static final long DAY = 360000000;

    /**
     * Transfer Byte to other unit
     */
    public static String transferByte(double v) {
        if (v < 1024) {
            return v + " B";
        }
        int z = (63 - Long.numberOfLeadingZeros((long) v)) / 10;
        return String.format("%.1f %sB", v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    /**
     * Transfer rows
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
     * Transfer vcore·s
     */
    public static String transferVcoreS(long data) {
        if (data > 3600) {
            return String.format("%dvcore·h", data / 3600);
        } else {
            return String.format("%dvcore·s", data);
        }
    }

    /**
     * Transfer memory·s
     */
    public static String transferMemGbS(long data) {
        if (data > 3600) {
            return String.format("%dGB·h", data / 3600);
        } else {
            return String.format("%dGB·s", data);
        }
    }

    /**
     * Transfer Byte to GB
     */
    public static double transferBToGB(Long v) {
        return v / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * Transfer KB to GB
     */
    public static double transferKBToGB(Long v) {
        return transferDouble(v / (1024.0 * 1024.0));
    }

    /**
     * Transfer Byte to MB
     */
    public static double transferBToMB(Long v) {
        return v / (1024.0 * 1024.0);
    }

    /**
     * Transfer MB to GB
     */
    public static double transferMBToGB(Long v) {
        return transferDouble(v / 1024.0);
    }

    /**
     * Transfer double
     */
    public static double transferDouble(double data) {
        double value = (double) Math.round(data * 100) / 100;
        return value == 0 ? (double) Math.round(data * 10000) / 10000 : value;
    }

    /**
     * Transfer timestamp to string
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
     * Transfer string to second
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
     * Get time unit
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
     * Convert cpu unit
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
     * Convert memorySeconds(MB-Seconds) unit
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
     * Add red color
     */
    public static String transferRed(String keyword) {
        return String.format("<span style=\"color: #e24a4a;\">%s</span>", keyword);
    }

    public static String transferRed(double keyword) {
        return String.format("<span style=\"color: #e24a4a;\">%.2f</span>", keyword);
    }

    /**
     * Transfer millisecond to date string
     *
     * @return
     */
    public static String transferTimeUnit(long millisecond) {
        List<String> lists = new ArrayList<>();

        if (millisecond > Day) {
            lists.add("" + millisecond / Day + "d");
            millisecond = millisecond % Day;
        }
        if (millisecond > Hour) {
            lists.add("" + millisecond / Hour + "h");
            millisecond = millisecond % Hour;
        }
        if (millisecond > Minute) {
            lists.add("" + millisecond / Minute + "m");
            millisecond = millisecond % Minute;
        }
        if (millisecond > Second) {
            lists.add("" + millisecond / Second + "s");
            millisecond = millisecond % Second;
        }
        if (lists.size() > 2) {
            return String.join("", lists.subList(0, 2));
        } else {
            return String.join("", lists);
        }
    }
}
