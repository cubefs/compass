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
