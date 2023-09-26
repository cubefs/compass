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

package com.oppo.cloud.detect.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Diagnostic tool.
 */
@Slf4j
public class DetectorUtil {

    /**
     * Box plot algorithm: Judging whether a number is an anomalous data based on the lower quartile, median,
     * and upper quartile of a set of historical data, and calculating the median, mildly anomalous value range,
     * and extremely anomalous value range of the box plot.
     */
    public static double[] boxplotValue(Double[] simpleData) throws Exception {
        double[] res = new double[5];
        double q1, q2, q3, iQR;
        try {
            double[] medianValue = sampleMedian(simpleData);
            q1 = medianValue[0];
            q3 = medianValue[2];
            q2 = medianValue[1];
            iQR = q3 - q1;
        } catch (Exception e) {
            throw new Exception(String.format("Failed to calculate the median value：%s, Sample：%s", Arrays.toString(e.getStackTrace()),
                    Arrays.toString(simpleData)));
        }
        res[0] = q1 - 3 * iQR;
        // 如果小于零则取最小值
        res[0] = res[0] > 0 ? res[0] : simpleData[0];
        res[1] = q1 - 1.5 * iQR;
        res[2] = q2;
        res[3] = q3 + 1.5 * iQR;
        res[4] = q3 + 3 * iQR;
        return res;
    }

    /**
     * Get the lower quartile, median, and upper quartile of a set of data.
     */
    public static double[] sampleMedian(Double[] data) {
        double[] res = new double[3];
        Arrays.sort(data);
        int len = (data.length - 1);
        if (len % 4 == 0) {
            res[0] = data[len / 4];
        } else {
            res[0] = (data[len / 4] + data[len / 4 + 1]) / 2;
        }
        if (len % 2 == 0) {
            res[1] = data[len / 2];
        } else {
            res[1] = (data[len / 2] + data[len / 2 + 1]) / 2;
        }
        if (len * 3 % 4 == 0) {
            res[2] = data[len * 3 / 4];
        } else {
            res[2] = (data[len * 3 / 4] + data[len * 3 / 4 + 1]) / 2;
        }

        return res;
    }

    /**
     * Dynamically convert the timestamp to a string of seconds, minutes, and hours.
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
     * Convert the timestamp to a time string according to the given time format.
     */

    public static String timeStampToStr(long timeUnix, String format) {
        try {
            if (StringUtils.isBlank(format)) {
                format = "yyyy-MM-dd'T'HH:mm:ss";
            }
            SimpleDateFormat timeFormat = new SimpleDateFormat(format);
            return timeFormat.format(new Date(timeUnix * 1000));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "";
    }

}
