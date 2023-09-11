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

public class Constant {

    /**
     * 小时毫秒数值
     */
    public static final long HOUR_MS = 60 * 60 * 1000;
    /**
     * 天秒数值
     */
    public static final long DAY_SECONDS = 24 * 60 * 60;
    /**
     * spark event log路径
     */
    public static final String SPARK_EVENT_LOG_DIRECTORY = "spark:event:log:directory:";
    /**
     * spark历史服务器缓存key
     */
    public static final String SPARK_HISTORY_SERVERS = "spark:history:servers";

    /**
     * yarn集群缓存key
     */
    public static final String YARN_CLUSTERS = "yarn:clusters";
    /**
     * resourceManager对应jobHistoryServer
     */
    public static final String RM_JHS_MAP = "rm:jhs:map";
    /**
     * jobHistoryServer conf: yarn.nodemanager.remote-app-log-dir
     */
    public static final String JHS_HDFS_PATH = "jhs:hdfs:path:";
    /**
     * yarn.app.mapreduce.am.staging-dir
     */
    public static final String JHS_MAPREDUCE_STAGING_PATH = "jhs:mapreduce:staging:path:";
    /**
     * mapreduce.jobhistory.done-dir
     */
    public static final String JHS_MAPREDUCE_DONE_PATH = "jhs:mapreduce:done:path:";
    /**
     * mapreduce.jobhistory.intermediate-done-dir
     */
    public static final String JHS_MAPREDUCE_INTERMEDIATE_DONE_PATH = "jhs:mapreduce:intermediate:done:path:";

    public static final String HDFS_SCHEME = "hdfs://";

}
