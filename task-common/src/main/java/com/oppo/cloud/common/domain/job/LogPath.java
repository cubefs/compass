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

import java.io.InputStream;

/**
 * LogPath Information
 */
@Data
public class LogPath {

    /**
     * Protocol: hdfs,s3
     */
    private String protocol;

    /**
     * LogType: scheduler,event,executor,gc
     */
    private String logType;

    /**
     * LogPathType: file,directory,pattern
     */
    private LogPathType logPathType;

    /**
     * LogPath
     */
    private String logPath;

    /*
     * InputStream fot this log
     */
    private InputStream inputStream;

    public LogPath() {

    }

    public LogPath(String protocol, String logType, LogPathType logPathType, String logPath) {
        this.protocol = protocol;
        this.logType = logType;
        this.logPathType = logPathType;
        this.logPath = logPath;
    }

    public LogPath(String protocol, String logType, InputStream inputStream) {
        this.protocol = protocol;
        this.logType = logType;
        this.inputStream = inputStream;
    }

}
