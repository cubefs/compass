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

/**
 * 异常类型枚举类
 */
public enum JobCategoryEnum {

    /**
     * 运行失败
     */
    executionFailed("运行失败", "执行失败的任务"),

    endTimeAbnormal("基线时间异常", "相对于历史正常结束时间，提前结束或者晚点结束的任务"),

    durationAbnormal("基线耗时异常", "相对于历史正常运行时长，运行时间过长或过短的任务"),

    firstFailed("首次失败", "重试次数大于1的成功任务"),

    alwaysFailed("长期失败", "近10天一直失败的任务"),

    durationLong("运行耗时长", "运行时长超过2个小时");

    private final String msg;
    private final String des;

    private static final Map<String, JobCategoryEnum> MAP;
    JobCategoryEnum(String msg, String des) {
        this.msg = msg;
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

    public String getMsg() {
        return msg;
    }



    /**
     * 获取作业异常类型的中文列表
     * @return
     */
    public static List<String> getAllAppCategoryOfChina() {
        List<String> res = new ArrayList<>();
        for (JobCategoryEnum jobCategoryEnum : JobCategoryEnum.values()) {
            res.add(jobCategoryEnum.getMsg());
        }
        return res;
    }

    /**
     * 获取作业异常类型,按enum顺序获取其中文列表
     *
     * @param categoryList
     * @return
     */
    public static List<String> getJobCategoryCh(List<String> categoryList) {
        List<String> res = new ArrayList<>();
        if (categoryList == null) {
            return res;
        }
        for (JobCategoryEnum jobCategory : JobCategoryEnum.values()) {
            if (categoryList.contains(jobCategory.name())) {
                res.add(jobCategory.getMsg());
            }
        }
        return res;
    }

    /**
     * 获取作业异常类型,按enum顺序获取其英文列表
     *
     * @param categoryChList
     * @return
     */
    public static List<String> getJobCategoryEn(List<String> categoryChList) {
        List<String> res = new ArrayList<>();
        if (categoryChList == null) {
            return res;
        }
        for (JobCategoryEnum jobCategory : JobCategoryEnum.values()) {
            if (categoryChList.contains(jobCategory.getMsg())) {
                res.add(jobCategory.name());
            }
        }
        return res;
    }

    /**
     * 获取作业异常类型的msg
     */
    public static String getJobNameMsg(String category) {
        if (MAP.containsKey(category)) {
            return MAP.get(category).getMsg();
        }
        return null;
    }
}
