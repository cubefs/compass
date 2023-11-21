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

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum AppCategoryEnum {

    // sql failed（syntax, field, permission, environment, etc）
    SQL_FAILED("sqlFailed", "sql失败", "runError", "sqlFailed"),

    // shuffle failed
    SHUFFLE_FAILED("shuffleFailed", "shuffle失败", "runError", "shuffleFailed"),

    // memory overflow
    MEMORY_OVERFLOW("memoryOverflow", "内存溢出", "runError", "memoryOverflow"),

    // memory waste
    MEMORY_WASTE("memoryWaste", "内存浪费", "resourceUsage", "memoryWaste"),

    // CPU waste
    CPU_WASTE("cpuWaste", "CPU浪费", "resourceUsage", "cpuWaste"),

    // large table scan
    LARGE_TABLE_SCAN("largeTableScan", "大表扫描", "runPerformance", "largeTableScan"),

    // oom warning
    OOMWarn("oomWarn", "OOM预警", "runPerformance", "oomWarn"),

    // data skew
    DATA_SKEW("dataSkew", "数据倾斜", "runPerformance", "dataSkew"),

    // Job duration abnormal
    JOB_DURATION("jobDurationAbnormal", "Job耗时异常", "runPerformance", "jobDurationAbnormal"),

    // Stage duration abnormal
    STAGE_DURATION("stageDurationAbnormal", "Stage耗时异常", "runPerformance", "stageDurationAbnormal"),

    // Task duration abnormal
    TASK_DURATION("taskDurationAbnormal", "Task长尾", "runPerformance", "taskDurationAbnormal"),

    // Hdfs stuck
    HDFS_STUCK("hdfsStuck", "HDFS卡顿", "runPerformance", "hdfsStuck"),

    // Speculative task is too many
    SPECULATIVE_TASK("speculativeTask", "推测执行Task过多", "runPerformance", "speculativeTask"),

    // global sorting exception
    GLOBAL_SORT("globalSortAbnormal", "全局排序异常", "runPerformance", "globalSortAbnormal"),

    // MapReduce memory waste
    MR_MEMORY_WASTE("mrMemoryWaste", "MR内存浪费", "resourceUsage", "mrMemoryWaste"),

    // MapReduce large table scan
    MR_LARGE_TABLE_SCAN("mrLargeTableScan", "MR大表扫描", "runPerformance", "mrLargeTableScan"),

    // MapReduce data skew
    MR_DATA_SKEW("mrDataSkew", "MR数据倾斜", "runPerformance", "mrDataSkew"),

    // MapReduce Task long tail
    MR_TASK_DURATION("mrTaskDurationAbnormal", "MRTask长尾", "runPerformance", "mrTaskDurationAbnormal"),

    // MapReduce speculative task is too many
    MR_SPECULATIVE_TASK("mrSpeculativeTask", "MR推测执行Task过多", "runPerformance", "mrSpeculativeTask"),

    // MapReduce GC exception
    MR_GC_ABNORMAL("mrGCAbnormal", "MRGC异常", "runPerformance", "mrGCAbnormal"),

    // other exception
    OTHER_EXCEPTION("otherException", "其他异常", "runError", "otherException");

    private final String category;
    private final String zh;
    private final String classify;
    private final String en;

    private static final Map<String, AppCategoryEnum> MAP;

    AppCategoryEnum(String category, String zh, String classify, String en) {
        this.category = category;
        this.zh = zh;
        this.classify = classify;
        this.en = en;
    }

    static {
        Map<String, AppCategoryEnum> map = new ConcurrentHashMap<>();
        for (AppCategoryEnum instance : AppCategoryEnum.values()) {
            map.put(instance.getCategory(), instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public String getZh() {
        return zh;
    }

    public String getClassify() {
        return classify;
    }

    public String getCategory() {
        return category;
    }

    public String getEn() {
        return en;
    }


    /**
     * Get the category name by language message
     */
    public static List<String> getCategoryByLangMsg(List<String> categoryList) {
        List<String> res = new ArrayList<>();
        if (categoryList == null) {
            return res;
        }
        for (AppCategoryEnum appCategory : AppCategoryEnum.values()) {
            String languageMsg;
            if (Locale.SIMPLIFIED_CHINESE.equals(LocaleContextHolder.getLocale())) {
                languageMsg = appCategory.getZh();
            } else {
                languageMsg = appCategory.getEn();
            }
            if (categoryList.contains(languageMsg)) {
                res.add(appCategory.getCategory());
            }
        }
        return res;
    }

    /**
     * Get language message by categories.
     */
    public static List<String> getLangMsgByCategories(List<String> categoryList) {
        List<String> res = new ArrayList<>();
        if (categoryList == null) {
            return res;
        }
        for (String category : categoryList) {
            if (!MAP.containsKey(category)) {
                continue;
            }
            if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
                res.add(MAP.get(category).getZh());
            } else {
                res.add(MAP.get(category).getEn());
            }
        }
        return res;
    }

    /**
     * Get the list of language message.
     */
    public static List<String> getAllLangMsg() {
        List<String> res = new ArrayList<>();
        for (AppCategoryEnum appCategory : AppCategoryEnum.values()) {
            if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
                res.add(appCategory.getZh());
            } else {
                res.add(appCategory.getEn());
            }

        }
        return res;
    }


    /**
     * Get the zh/en of app exception types.
     */
    public static String getAppCategory(String category) {
        if (MAP.containsKey(category)) {
            if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
                return MAP.get(category).getZh();
            } else {
                return MAP.get(category).getEn();
            }
        }
        return null;
    }

}
