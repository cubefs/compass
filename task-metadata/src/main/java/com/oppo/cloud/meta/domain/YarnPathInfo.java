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

package com.oppo.cloud.meta.domain;

import lombok.Data;

@Data
public class YarnPathInfo {
    /**
     * fs.defaultFS
     */
    private String defaultFS;

    /**
     * yarn.nodemanager.remote-app-log-dir
     */
    private String remoteDir;

    /**
     * yarn.app.mapreduce.am.staging-dir
     */
    private String mapreduceStagingDir;

    /**
     * mapreduce.jobhistory.done-dir
     */
    private String mapreduceDoneDir;

    /**
     * mapreduce.jobhistory.intermediate-done-dir
     */
    private String mapreduceIntermediateDoneDir;
}
