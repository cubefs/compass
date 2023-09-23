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

package com.oppo.cloud.portal.domain.statistics;

import com.oppo.cloud.common.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 周期开始和结束时间戳
 */
@Data
@NoArgsConstructor
public class PeriodTime {
    private long startTimestamp;

    private long endTimestamp;


    public PeriodTime(int day) {
        this.endTimestamp = DateUtil.todayZero();
        this.startTimestamp = day * endTimestamp - 24 * 3600 * 1000L;
    }

    public PeriodTime getLastDayPeriod() {
        PeriodTime periodTime = new PeriodTime();
        periodTime.setStartTimestamp(this.startTimestamp - 24 * 3600 * 1000L);
        periodTime.setEndTimestamp(this.endTimestamp - 24 * 3600 * 1000L);
        return periodTime;
    }


    public PeriodTime getLastWeekPeriod() {
        PeriodTime periodTime = new PeriodTime();
        periodTime.setStartTimestamp(this.startTimestamp - 7 * 24 * 3600 * 1000L);
        periodTime.setEndTimestamp(this.endTimestamp - 7 * 24 * 3600 * 1000L);
        return periodTime;
    }
}
