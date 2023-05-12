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

import java.util.Calendar;

public class MRUtil {

    public static final int SERIAL_NUMBER_DIRECTORY_DIGITS = 6;

    private static final String TIMESTAMP_DIR_FORMAT = "%04d/%02d/%02d";

    private static final String SERIAL_NUMBER_FORMAT = "%09d";

    private static final String SUB_DIR_FORMAT = "%s/%s/";

    public static String historyLogSubdirectory(String id, long finishedTime) {
        String timestampComponent = timestampDirectoryComponent(finishedTime);
        String serialNumberDirectory = serialNumberDirectoryComponent(id);
        return String.format(SUB_DIR_FORMAT, timestampComponent, serialNumberDirectory);
    }

    public static String serialNumberDirectoryComponent(String id) {
        return String.format(SERIAL_NUMBER_FORMAT, jobSerialNumber(id)).substring(0, SERIAL_NUMBER_DIRECTORY_DIGITS);
    }

    public static int jobSerialNumber(String id) {
        return Integer.parseInt(id.substring(id.lastIndexOf('_') + 1));
    }

    public static String timestampDirectoryComponent(long millisecondTime) {
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(millisecondTime);
        String dateString;
        dateString = String.format(TIMESTAMP_DIR_FORMAT,
                timestamp.get(Calendar.YEAR),
                timestamp.get(Calendar.MONTH) + 1,
                timestamp.get(Calendar.DAY_OF_MONTH));
        dateString = dateString.intern();
        return dateString;
    }
}
