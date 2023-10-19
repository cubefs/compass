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

package com.oppo.cloud.portal.domain.flink;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oppo.cloud.common.util.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Data
@Slf4j
public class DiagnosisGeneralViewReq {
    /**
     * start timestamp
     */
    private Long startTs;
    /**
     * end timestamp
     */
    private Long endTs;

    @JsonIgnore
    public HashMap<String, Object[]> getRangeConditions() {
        if (startTs == null || endTs == null || startTs == 0 || endTs == 0) {
            this.endTs = System.currentTimeMillis();
            this.startTs = this.endTs - 30 * 24 * 3600 * 1000L;
        }

        if (startTs.toString().length() < 13) { // to millis
            startTs *= 1000;
            endTs *= 1000;
        }

        HashMap<String, Object[]> rangeConditions = new HashMap<>();
        Object[] values = new Object[2];
        if (startTs != 0) {
            values[0] = DateUtil.timestampToUTCDate(startTs);
        }
        if (endTs != 0) {
            values[1] = DateUtil.timestampToUTCDate(endTs);
        }
        rangeConditions.put("createTime", values);

        return rangeConditions;
    }
}
