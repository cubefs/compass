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

package com.oppo.cloud.common.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum AppCategoryEnum {

    // sql failed（syntax, field, permission, environment, etc）
    SQL_FAILED("sqlFailed", "sql失败", "runError"),

    // shuffle failed
    SHUFFLE_FAILED("shuffleFailed", "shuffle失败", "runError"),

    // memory overflow
    MEMORY_OVERFLOW("memoryOverflow", "内存溢出", "runError"),

    // memory waste
    MEMORY_WASTE("memoryWaste", "内存浪费", "resourceUsage"),

    // CPU waste
    CPU_WASTE("cpuWaste", "CPU浪费", "resourceUsage"),

    // large table scan
    LARGE_TABLE_SCAN("largeTableScan", "大表扫描", "runPerformance"),

    // oom warning
    OOMWarn("oomWarn", "OOM预警", "runPerformance"),

    // data skew
    DATA_SKEW("dataSkew", "数据倾斜", "runPerformance"),

    // Job duration abnormal
    JOB_DURATION("jobDurationAbnormal", "Job耗时异常", "runPerformance"),

    // Stage duration abnormal
    STAGE_DURATION("stageDurationAbnormal", "Stage耗时异常", "runPerformance"),

    // Task duration abnormal
    TASK_DURATION("taskDurationAbnormal", "Task长尾", "runPerformance"),

    // Hdfs stuck
    HDFS_STUCK("hdfsStuck", "HDFS卡顿", "runPerformance"),

    // Speculative task is too many
    SPECULATIVE_TASK("speculativeTask", "推测执行Task过多", "runPerformance"),

    // global sorting exception
    GLOBAL_SORT("globalSortAbnormal", "全局排序异常", "runPerformance"),

    // MapReduce memory waste
    MR_MEMORY_WASTE("mrMemoryWaste", "MR内存浪费", "resourceUsage"),

    // MapReduce large table scan
    MR_LARGE_TABLE_SCAN("mrLargeTableScan", "MR大表扫描", "runPerformance"),

    // MapReduce data skew
    MR_DATA_SKEW("mrDataSkew", "MR数据倾斜", "runPerformance"),

    // MapReduce Task long tail
    MR_TASK_DURATION("mrTaskDurationAbnormal", "MRTask长尾", "runPerformance"),

    // MapReduce speculative task is too many
    MR_SPECULATIVE_TASK("mrSpeculativeTask", "MR推测执行Task过多", "runPerformance"),

    // MapReduce GC exception
    MR_GC_ABNORMAL("mrGCAbnormal", "MRGC异常", "runPerformance"),

    // other exception
    OTHER_EXCEPTION("otherException", "其他异常", "runError");

    private final String category;
    private final String desc;
    private final String classify;

    private static final Map<String, AppCategoryEnum> MAP;

    AppCategoryEnum(String category, String desc, String classify) {
        this.category = category;
        this.desc = desc;
        this.classify = classify;
    }

    static {
        Map<String, AppCategoryEnum> map = new ConcurrentHashMap<>();
        for (AppCategoryEnum instance : AppCategoryEnum.values()) {
            map.put(instance.getCategory(), instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public String getDesc() {
        return desc;
    }

    public String getClassify() {
        return classify;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Get the exception type of the app and get the Chinese list in the order of the enum
     *
     * @param categoryList
     * @return
     */
    public static List<String> getAppCategoryCh(List<String> categoryList) {
        List<String> res = new ArrayList<>();
        if (categoryList == null) {
            return res;
        }
        for (AppCategoryEnum appCategory : AppCategoryEnum.values()) {
            if (categoryList.contains(appCategory.getCategory())) {
                res.add(appCategory.getDesc());
            }
        }
        return res;
    }

    /**
     * Get the exception type of the app and get the English list in the order of the enum.
     *
     * @param categoryChList
     * @return
     */
    public static List<String> getAppCategoryEn(List<String> categoryChList) {
        List<String> res = new ArrayList<>();
        if (categoryChList == null) {
            return res;
        }
        for (AppCategoryEnum appCategory : AppCategoryEnum.values()) {
            if (categoryChList.contains(appCategory.getDesc())) {
                res.add(appCategory.getCategory());
            }
        }
        return res;
    }

    /**
     * Get the list of app exception types.
     *
     * @return
     */
    public static List<String> getAllAppCategoryOfChina() {
        List<String> res = new ArrayList<>();
        for (AppCategoryEnum appCategory : AppCategoryEnum.values()) {
            res.add(appCategory.getDesc());
        }
        return res;
    }

    /**
     * Get the Chinese name of app exception types.
     */
    @Deprecated
    public static String getAppCategoryOfChina(String category) {
        for (AppCategoryEnum appCategory : AppCategoryEnum.values()) {
            if (appCategory.category.equals(category)) {
                return appCategory.getDesc();
            }
        }
        return "";
    }

    /**
     * Get the description of app exception types.
     */
    public static String getAppCategoryOfDesc(String category) {
        if (MAP.containsKey(category)) {
            return MAP.get(category).getDesc();
        }
        return null;
    }

}
