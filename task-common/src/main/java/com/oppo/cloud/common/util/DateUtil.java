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

package com.oppo.cloud.common.util;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;


public class DateUtil {

    private static Map<String, ThreadLocal<SimpleDateFormat>> thredlocalmap =
            new Hashtable<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getDateFormat(String datePattern) {
        ThreadLocal<SimpleDateFormat> theadlocal = thredlocalmap.get(datePattern);
        if (theadlocal == null) {
            theadlocal = ThreadLocal.withInitial(() -> new SimpleDateFormat(datePattern));
            thredlocalmap.put(datePattern, theadlocal);
        }
        return theadlocal.get();
    }

    public static Date parseStrToDate(String timeStr) {
        return parseStrToDate(timeStr, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date parseStrToDate(String timeStr, String pattern) {
        SimpleDateFormat sdf = getDateFormat(pattern);
        try {
            return sdf.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String format(Date date, String datePattern) {
        return getDateFormat(datePattern).format(date);
    }


    public static String format(Date date) {
        return getDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


    public static String formatToDay(Date date) {
        return getDateFormat("yyyy-MM-dd").format(date);
    }


    public static long dateToTimeStamp(String dateStr) {
        Date date = parseStrToDate(dateStr);
        return date.getTime() / 1000;
    }

    /**
     * 本地时间戳转UTC时间
     */
    public static String timestampToUTCDate(long time) {
        Calendar ca = Calendar.getInstance();
        ca.setTimeInMillis(time);
        Date source = ca.getTime();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(source);
    }

    /**
     * 本地时间戳转UTC时间格式
     */
    public static String timestampToUTCStr(long time) {
        Calendar ca = Calendar.getInstance();
        ca.setTimeInMillis(time);
        Date source = ca.getTime();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return sdf.format(source);
    }

    /**
     * 时间简化： end - start
     *
     * @param start 开始时间
     * @param end   结束时间
     */
    public static String timeSimplify(Date start, Date end) {
        long s = 0L, e = 0L;
        if (start != null) {
            s = start.getTime();
        }
        if (end != null) {
            e = end.getTime();
        }
        return timeSimplify((e - s) / 1000.0);
    }

    /**
     * 时间简化
     */
    public static String timeSimplify(Double timeDelta) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if (timeDelta < 60) {
            return decimalFormat.format(timeDelta) + "s";
        } else if (timeDelta < 3600) {
            return decimalFormat.format(timeDelta / 60) + "m";
        } else if (timeDelta < 24 * 3600) {
            return decimalFormat.format(timeDelta / 3600) + "h";
        } else {
            return decimalFormat.format(timeDelta / (24 * 3600)) + "d";
        }
    }

    /**
     * 获取相对于偏移量为offset的时间
     */
    public static Date getOffsetDate(Date date, Integer offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, offset);
        return calendar.getTime();
    }

    /**
     * 获取当天零点的时间戳
     */
    public static Long todayZero() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取某一天零点的时间戳
     */
    public static long dateOfZeroDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取某一天yyyy-MM-dd格式字符换，例如：当天amount=0,昨天amount=-1
     */
    public static String getDay(int amount) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(cal.getTime());
        cal.add(Calendar.DATE, amount);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(cal.getTime());
    }

    /**
     * 获取 fastjson2 utc context
     */
    public static JSONReader.Context getUTCContext() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneId.of("UTC"));
        return context;
    }
}
