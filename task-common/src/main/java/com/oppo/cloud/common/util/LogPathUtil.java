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

package com.oppo.cloud.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.constant.YarnAppState;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.mr.MRJobHistoryLogPath;
import com.oppo.cloud.common.service.RedisService;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Map;

public class LogPathUtil {

    public static final int MR_SERIAL_NUMBER_DIRECTORY_DIGITS = 6;

    private static final String MR_TIMESTAMP_DIR_FORMAT = "%04d/%02d/%02d";

    private static final String MR_SERIAL_NUMBER_FORMAT = "%09d";

    private static final String MR_SUB_DIR_FORMAT = "%s/%s/";

    private static final String MR_JOB_HISTORY_LOG_DIR_FORMAT = "%s/%s/.staging/%s/%s*";

    public static final String SPARK_EVENT_LOG_RUNNING_EXTENSION = ".inprogress";


    public static MRJobHistoryLogPath getMRJobHistoryDoneLogPath(YarnApp yarnApp, RedisService redisService) throws Exception {
        MRJobHistoryLogPath mrJobHistoryLogPath = new MRJobHistoryLogPath();
        String jobHistoryDoneLogPathPrefix = getYarnLogPath(Constant.JHS_MAPREDUCE_DONE_PATH, yarnApp.getIp(), redisService);
        if ("".equals(jobHistoryDoneLogPathPrefix)) {
            throw new Exception(String.format("can not find mr job history log path: rm ip : %s", yarnApp.getIp()));
        }
        String logSubdirectory = getHistoryLogSubdirectory(yarnApp.getId(), yarnApp.getFinishedTime());
        String jobId = appIdToJobId(yarnApp.getId());
        String jobHistoryDoneLogPath = String.format("%s/%s%s*", jobHistoryDoneLogPathPrefix, logSubdirectory, jobId);
        mrJobHistoryLogPath.setDoneLogPath(jobHistoryDoneLogPath);
        String jobHistoryIntermediateDoneLogPathPrefix = getYarnLogPath(Constant.JHS_MAPREDUCE_INTERMEDIATE_DONE_PATH, yarnApp.getIp(), redisService);
        String jobHistoryIntermediateDoneLogPath = String.format("%s/%s/%s*", jobHistoryIntermediateDoneLogPathPrefix, yarnApp.getUser(), jobId);
        mrJobHistoryLogPath.setIntermediateDoneLogPath(jobHistoryIntermediateDoneLogPath);
        if (YarnAppState.RUNNING.toString().equals(yarnApp.getState())) {
            String stagingLogPathPrefix = getYarnLogPath(Constant.JHS_MAPREDUCE_STAGING_PATH, yarnApp.getIp(), redisService);
            String stagingLogPath = String.format(MR_JOB_HISTORY_LOG_DIR_FORMAT, stagingLogPathPrefix, yarnApp.getUser(), jobId, jobId);
            mrJobHistoryLogPath.setStagingLogPath(stagingLogPath);
        }
        return mrJobHistoryLogPath;
    }

    public static String getHistoryLogSubdirectory(String appId, long finishedTime) {
        String timestampComponent = timestampDirectoryComponent(finishedTime);
        String serialNumberDirectory = serialNumberDirectoryComponent(appId);
        return String.format(MR_SUB_DIR_FORMAT, timestampComponent, serialNumberDirectory);
    }

    public static String serialNumberDirectoryComponent(String appId) {
        return String.format(MR_SERIAL_NUMBER_FORMAT, jobSerialNumber(appId)).substring(0, MR_SERIAL_NUMBER_DIRECTORY_DIGITS);
    }

    public static int jobSerialNumber(String appId) {
        return Integer.parseInt(appId.substring(appId.lastIndexOf('_') + 1));
    }

    public static String timestampDirectoryComponent(long millisecondTime) {
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTimeInMillis(millisecondTime);
        String dateString;
        dateString = String.format(MR_TIMESTAMP_DIR_FORMAT,
                timestamp.get(Calendar.YEAR),
                timestamp.get(Calendar.MONTH) + 1,
                timestamp.get(Calendar.DAY_OF_MONTH));
        dateString = dateString.intern();
        return dateString;
    }

    public static String appIdToJobId(String appId) {
        if (appId == null) {
            return "";
        }
        return appId.replaceFirst("^application_", "job_");
    }

    /**
     * get the redis cache from jobhistory server conf:
     * 1. yarn.nodemanager.remote-app-log-dir
     * 2. mapreduce.jobhistory.done-dir
     * 3. mapreduce.jobhistory.intermediate-done-dir
     */
    public static String getYarnLogPath(String logType, String rmIp, RedisService redisService) throws Exception {
        if (!redisService.hasKey(Constant.RM_JHS_MAP)) {
            throw new Exception(String.format("search redis error,msg: can not find key %s", Constant.RM_JHS_MAP));
        }
        Map<String, String> rmJhsMap = JSON.parseObject((String) redisService.get(Constant.RM_JHS_MAP),
                new TypeReference<Map<String, String>>() {
                });
        String jhsIp = rmJhsMap.get(rmIp);
        String key = logType + jhsIp;
        if (!redisService.hasKey(key)) {
            throw new Exception(String.format("search redis error,msg: can not find key %s, rmJhsMap:%s, rmIp:%s",
                    key, rmJhsMap, rmIp));
        }
        return (String) redisService.get(key);
    }

    public static String getSparkEventLogPath(String prefixDir, String appId, String attemptId, String state) {
        String eventLogPath = String.format("%s/%s", prefixDir, appId);
        if (!StringUtils.isEmpty(attemptId)) {
            eventLogPath = String.format("%s_%s", eventLogPath, attemptId);
        }
        if (YarnAppState.RUNNING.toString().equals(state)) {
            eventLogPath = String.format("%s%s", eventLogPath, SPARK_EVENT_LOG_RUNNING_EXTENSION);
        }
        return eventLogPath;
    }

}
