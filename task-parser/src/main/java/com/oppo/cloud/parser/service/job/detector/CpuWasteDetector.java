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

package com.oppo.cloud.parser.service.job.detector;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.CpuWasteAbnormal;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.CpuWasteConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.job.ExecutorTimeSpan;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkApplication;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
public class CpuWasteDetector implements IDetector {

    private final DetectorParam param;

    private final CpuWasteConfig config;

    CpuWasteDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getCpuWasteConfig();
    }

    @Override
    public DetectorResult detect() {

        DetectorResult<CpuWasteAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.CPU_WASTE.getCategory(), false);
        SparkApplication application = this.param.getReplayEventLogs().getApplication();

        long appTotalTime = application.getAppEndTimestamp() - application.getAppStartTimestamp();
        if (appTotalTime < 0) {
            log.warn("appTotalTime:{},{}", this.param.getAppId(), appTotalTime);
            return null;
        }

        long jobTime = estimatedTimeSpentInJobs();

        long maxExecutors = getMaxConcurrent();
        long executorCores = Long.parseLong(application.getSparkExecutorCores());
        long totalCores = executorCores * maxExecutors;

        long appComputeMillisAvailable = totalCores * appTotalTime;
        long inJobComputeMillisAvailable = totalCores * jobTime;

        if (inJobComputeMillisAvailable == 0) {
            return null;
        }

        long inJobComputeMillisUsed = getInJobComputeMillisUsed();

        long driverTimeJobBased = appTotalTime - jobTime;
        long driverComputeMillisWastedJobBased = driverTimeJobBased * totalCores;

        float driverWastedPercentOverAll =
                ((float) driverComputeMillisWastedJobBased / appComputeMillisAvailable) * 100;

        float executorWastedPercentOverAll =
                (((float) inJobComputeMillisAvailable - inJobComputeMillisUsed) / appComputeMillisAvailable) * 100;

        float driverThreshold = this.config.getDriverThreshold();
        float executorThreshold = this.config.getExecutorThreshold();
        CpuWasteAbnormal cpuWasteAbnormal = new CpuWasteAbnormal();
        cpuWasteAbnormal.setAppComputeMillisAvailable(appComputeMillisAvailable);
        cpuWasteAbnormal.setInJobComputeMillisAvailable(inJobComputeMillisAvailable);
        cpuWasteAbnormal.setInJobComputeMillisUsed(inJobComputeMillisUsed);
        cpuWasteAbnormal.setDriverWastedPercentOverAll(driverWastedPercentOverAll);
        cpuWasteAbnormal.setExecutorWastedPercentOverAll(executorWastedPercentOverAll);

        cpuWasteAbnormal.setMaxExecutors(maxExecutors);
        cpuWasteAbnormal.setExecutorCores(executorCores);

        if (driverWastedPercentOverAll < 0 || executorWastedPercentOverAll < 0) {
            log.error("CpuWasteDetectErr:{},{}", this.param.getAppId(), cpuWasteAbnormal);
            return null;
        }
        if ((executorWastedPercentOverAll > executorThreshold || driverWastedPercentOverAll > driverThreshold) &&
                this.param.getAppDuration() > this.config.getDuration()) {
            detectorResult.setAbnormal(true);
        }
        detectorResult.setData(cpuWasteAbnormal);
        return detectorResult;
    }

    private long estimatedTimeSpentInJobs() {
        List<SparkJob> lists = new ArrayList<>();
        for (Map.Entry<Integer, SparkJob> jobs : this.param.getReplayEventLogs().getJobs().entrySet()) {
            lists.add(jobs.getValue());
        }
        if (lists.size() == 0) {
            return 0;
        }
        lists.sort(Comparator.comparing(SparkJob::getSubmissionTime));
        // job累计时间
        long jobAcc = 0;
        SparkJob cur = lists.get(0);
        for (int i = 1; i < lists.size(); i++) {
            SparkJob jobInfo = lists.get(i);
            if (cur.getEndTime() <= jobInfo.getSubmissionTime()) {
                jobAcc += cur.getEndTime() - cur.getSubmissionTime();
            }
            if (jobInfo.getEndTime() > cur.getEndTime()) {
                cur = jobInfo;
            }
        }
        jobAcc += cur.getEndTime() - cur.getSubmissionTime();
        return jobAcc;
    }

    private long getMaxConcurrent() {
        List<ExecutorTimeSpan> executorTimeSpans = new ArrayList<>();
        this.param.getReplayEventLogs().getExecutors().values().forEach(sparkExecutor -> {
            long correctedEndTime;
            if (sparkExecutor.getRemoveTimestamp() == 0) {
                if (this.param.getReplayEventLogs().getApplication().getAppEndTimestamp() == 0) {
                    correctedEndTime = System.currentTimeMillis();
                } else {
                    correctedEndTime = this.param.getReplayEventLogs().getApplication().getAppEndTimestamp();
                }
            } else {
                correctedEndTime = sparkExecutor.getRemoveTimestamp();
            }
            executorTimeSpans.add(new ExecutorTimeSpan(sparkExecutor.getStartTimestamp(), 1L));
            executorTimeSpans.add(new ExecutorTimeSpan(correctedEndTime, -1L));
        });
        executorTimeSpans.sort((o1, o2) -> {
            if (o1.getStartTime().equals(o2.getStartTime())) {
                return Integer.compare(o1.getEndTime().compareTo(o2.getEndTime()), 0);
            } else {
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });
        long count = 0L;
        long maxConcurrent = 0L;

        for (ExecutorTimeSpan span : executorTimeSpans) {
            count += span.getEndTime();
            maxConcurrent = Math.max(maxConcurrent, count);
        }

        return Math.max(maxConcurrent, 1);
    }

    private long getInJobComputeMillisUsed() {
        long sum = 0L;
        for (Map.Entry<Integer, SparkJob> entry : this.param.getReplayEventLogs().getJobs().entrySet()) {
            sum += entry.getValue().getExecutorRunTime();
        }
        return sum;
    }
}
