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

package com.oppo.cloud.common.util.ui;


public class UIUtil {

    // Chart color
    // Exception type color: red
    public static final String ABNORMAL_COLOR = "#f60000";

    public static final String ABNORMAL_COLOR_2 = "#F08080";
    // Regular type color: blue
    public static final String NORMAL_COLOR = "#6cdcd5";
    // Key type color: green
    public static final String KEY_COLOR = "#00b500";
    // Plain type color: yellow
    public static final String PLAIN_COLOR = "#ffc84f";

    /**
     * String color with red tag
     */
    public static String transferRed(String keyword) {
        return String.format("<span style=\"color: #e24a4a;\">%s</span>", keyword);
    }

    public static String transferRed(double keyword) {
        return String.format("<span style=\"color: #e24a4a;\">%.2f</span>", keyword);
    }

    /**
     * Hyperlink
     */
    public static String transferHyperLink(String url, String keyword) {
        return String.format("<a target=\"_blank\" style=\"color: rgb(45, 204, 195);\" " +
                "href=\"%s\">%s</a>", url, keyword);
    }
}
