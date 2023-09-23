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

package com.oppo.cloud.common.domain.flink.enums;

import lombok.Getter;

/**
 * yarn app 状态
 */
@Getter
public enum YarnApplicationState {
    /**
     * yarn app 新建状态
     */
    NEW(0, "NEW"),
    /**
     * yarn app NEW_SAVING 状态
     */
    NEW_SAVING(1, "NEW_SAVING"),
    /**
     * yarn app SUBMITTED 状态
     */
    SUBMITTED(2, "SUBMITTED"),
    /**
     * yarn app ACCEPTED 状态
     */
    ACCEPTED(3, "ACCEPTED"),
    /**
     * yarn app RUNNING 状态
     */
    RUNNING(4, "RUNNING"),
    /**
     * yarn app FINISHED 状态
     */
    FINISHED(5, "FINISHED"),
    /**
     * yarn app FAILED 状态
     */
    FAILED(6, "FAILED"),
    /**
     * yarn app KILLED 状态
     */
    KILLED(7, "KILLED"),
    ;

    private final int code;
    private final String desc;

    YarnApplicationState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
