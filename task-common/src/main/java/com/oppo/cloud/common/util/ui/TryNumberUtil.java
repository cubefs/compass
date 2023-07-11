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

import com.oppo.cloud.common.constant.SchedulerType;

public class TryNumberUtil {

    /**
     * 不同平台重试次数定义不一样，统一从0开始
     * DolphinScheduler是从0开始
     * Airflow是从1开始
     */
    public static int updateTryNumber(int tryNumber, String schedulerType) {
        if (SchedulerType.Airflow.toString().equalsIgnoreCase(schedulerType)) {
            if (tryNumber >= 1) {
                return tryNumber - 1;
            }
        }
        return tryNumber;
    }
}
