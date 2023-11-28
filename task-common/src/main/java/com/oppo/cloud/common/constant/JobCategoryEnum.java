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

/**
 * Exception type enumeration class.
 */
public enum JobCategoryEnum {

    /**
     * Failed to run.
     */
    executionFailed("运行失败", "executionFailed", "执行失败的任务"),

    endTimeAbnormal("基线时间异常", "endTimeAbnormal", "相对于历史正常结束时间，提前结束或者晚点结束的任务"),

    durationAbnormal("基线耗时异常", "durationAbnormal", "相对于历史正常运行时长，运行时间过长或过短的任务"),

    firstFailed("首次失败", "firstFailed", "重试次数大于1的成功任务"),

    alwaysFailed("长期失败", "alwaysFailed", "近10天一直失败的任务"),

    durationLong("运行耗时长", "durationLong", "运行时长超过2个小时");

    private final String zh;
    private final String en;
    private final String des;

    private static final Map<String, JobCategoryEnum> MAP;

    JobCategoryEnum(String zh, String en, String des) {
        this.zh = zh;
        this.en = en;
        this.des = des;
    }

    static {
        Map<String, JobCategoryEnum> map = new ConcurrentHashMap<>();
        for (JobCategoryEnum instance : JobCategoryEnum.values()) {
            map.put(instance.name(), instance);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public String getDes() {
        return des;
    }

    public String getZh() {
        return zh;
    }

    public String getEn() {
        return en;
    }

    /**
     * Get the Chinese list of job exception types.
     *
     * @return
     */
    public static List<String> getAllLangMsg() {
        List<String> res = new ArrayList<>();
        for (JobCategoryEnum jobCategoryEnum : JobCategoryEnum.values()) {
            if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
                res.add(jobCategoryEnum.getZh());
            } else {
                res.add(jobCategoryEnum.getEn());
            }
        }
        return res;
    }


    /**
     * Get the category by language message.
     */
    public static List<String> getCategoryByLangMsg(List<String> categoryList) {
        List<String> res = new ArrayList<>();
        if (categoryList == null) {
            return res;
        }
        for (JobCategoryEnum jobCategory : JobCategoryEnum.values()) {
            String languageMsg;
            if (Locale.SIMPLIFIED_CHINESE.equals(LocaleContextHolder.getLocale())) {
                languageMsg = jobCategory.getZh();
            } else {
                languageMsg = jobCategory.getEn();
            }
            if (categoryList.contains(languageMsg)) {
                res.add(jobCategory.name());
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
     * Get language message by category.
     */
    public static String getLangMsgByCategory(String category) {
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
