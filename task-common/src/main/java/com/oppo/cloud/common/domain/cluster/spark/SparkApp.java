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

package com.oppo.cloud.common.domain.cluster.spark;

import com.oppo.cloud.common.domain.cluster.yarn.Attempt;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * sparK app entity
 */
@Data
public class SparkApp {

    /**
     * spark app id
     */
    private String appId;
    /**
     * attempt id
     */
    private String attemptId;
    /**
     * duration
     */
    private Integer duration;
    /**
     * start time (epoch)
     */
    private Long startTimeEpoch;
    /**
     * end time (epoch)
     */
    private Long endTimeEpoch;
    /**
     * spark history server ip
     */
    private String sparkHistoryServer;
    /**
     * spark event log  hdfs directory
     */
    private String eventLogDirectory;
    /**
     * save time
     */
    private Long createTime;

    public SparkApp() {

    }

    public SparkApp(String appId, String eventLogDirectory, Attempt attempt, String sparkHistoryInfo) {
        this.appId = appId;
        this.attemptId = attempt.getAttemptId();
        this.duration = attempt.getDuration();
        this.startTimeEpoch = attempt.getStartTimeEpoch();
        this.endTimeEpoch = attempt.getEndTimeEpoch();
        this.sparkHistoryServer = sparkHistoryInfo;
        this.eventLogDirectory = eventLogDirectory;
        this.createTime = System.currentTimeMillis();
    }

    public Map<String, Object> getSparkAppMap() {
        Map<String, Object> m = new HashMap<>(8);
        m.put("appId", this.appId);
        m.put("attemptId", this.attemptId);
        m.put("duration", this.duration);
        m.put("startTimeEpoch", this.startTimeEpoch);
        m.put("endTimeEpoch", this.endTimeEpoch);
        m.put("sparkHistoryServer", this.sparkHistoryServer);
        m.put("eventLogDirectory", this.eventLogDirectory);
        m.put("createTime", this.createTime);
        return m;
    }
}
