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

    // sql失败（语法，字段，权限，环境等）
    SQL_FAILED("sqlFailed", "sql失败", "runError"),

    // shuffle失败
    SHUFFLE_FAILED("shuffleFailed", "shuffle失败", "runError"),

    // 内存溢出
    MEMORY_OVERFLOW("memoryOverflow", "内存溢出", "runError"),

    // 内存浪费
    MEMORY_WASTE("memoryWaste", "内存浪费", "resourceUsage"),

    // CPU浪费
    CPU_WASTE("cpuWaste", "CPU浪费", "resourceUsage"),

    // 大表扫描
    LARGE_TABLE_SCAN("largeTableScan", "大表扫描", "runPerformance"),

    // oom预警
    OOMWarn("oomWarn", "OOM预警", "runPerformance"),

    // 数据倾斜
    DATA_SKEW("dataSkew", "数据倾斜", "runPerformance"),

    // Job耗时异常
    JOB_DURATION("jobDurationAbnormal", "Job耗时异常", "runPerformance"),

    // Stage耗时异常
    STAGE_DURATION("stageDurationAbnormal", "Stage耗时异常", "runPerformance"),

    // Task耗时异常
    TASK_DURATION("taskDurationAbnormal", "Task长尾", "runPerformance"),

    // Hdfs卡顿
    HDFS_STUCK("hdfsStuck", "HDFS卡顿", "runPerformance"),

    // 推测执行Task过多
    SPECULATIVE_TASK("speculativeTask", "推测执行Task过多", "runPerformance"),

    // 全局排序异常
    GLOBAL_SORT("globalSortAbnormal", "全局排序异常", "runPerformance"),

    // MapReduce内存浪费
    MR_MEMORY_WASTE("mrMemoryWaste", "MR内存浪费", "resourceUsage"),

    // MapReduce大表扫描
    MR_LARGE_TABLE_SCAN("mrLargeTableScan", "MR大表扫描", "runPerformance"),

    // MapReduce数据倾斜
    MR_DATA_SKEW("mrDataSkew", "MR数据倾斜", "runPerformance"),

    // MapReduce Task长尾
    MR_TASK_DURATION("mrTaskDurationAbnormal", "MRTask长尾", "runPerformance"),

    // MapReduce推测执行Task过多
    MR_SPECULATIVE_TASK("mrSpeculativeTask", "MR推测执行Task过多", "runPerformance"),

    // MapReduce GC异常
    MR_GC_ABNORMAL("mrGCAbnormal", "MRGC异常", "runPerformance"),

    // 其他异常
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
     * 获取app异常类型,按enum顺序获取其中文列表
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
     * 获取app异常类型,按enum顺序获取其英文列表
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
     * 获取app异常类型的中文列表
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
     * 获取app异常类型的中文名称
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
     * 获取app异常类型的desc
     */
    public static String getAppCategoryOfDesc(String category) {
        if (MAP.containsKey(category)) {
            return MAP.get(category).getDesc();
        }
        return null;
    }

}
