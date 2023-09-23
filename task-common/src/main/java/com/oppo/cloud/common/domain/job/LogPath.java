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

package com.oppo.cloud.common.domain.job;

import com.oppo.cloud.common.constant.LogPathType;
import lombok.Data;

/**
 * 日志信息
 */
@Data
public class LogPath {

    /**
     * 协议: hdfs,s3
     */
    private String protocol;

    /**
     * 类型: scheduler,event,executor,gc
     */
    private String logType;

    /**
     * 日志路径类型: file,directory,pattern
     */
    private LogPathType logPathType;

    /**
     * 路径
     */
    private String logPath;

    public LogPath() {

    }

    public LogPath(String protocol, String logType, LogPathType logPathType, String logPath) {
        this.protocol = protocol;
        this.logType = logType;
        this.logPathType = logPathType;
        this.logPath = logPath;
    }

}
