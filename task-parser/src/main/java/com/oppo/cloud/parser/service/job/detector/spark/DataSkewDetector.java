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

package com.oppo.cloud.parser.service.job.detector.spark;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.DataSkewAbnormal;
import com.oppo.cloud.common.domain.eventlog.DataSkewGraph;
import com.oppo.cloud.common.domain.eventlog.GraphType;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.config.DataSkewConfig;
import com.oppo.cloud.common.domain.eventlog.config.MedianInterval;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

/**
 * Data Skew Detector
 */
@Slf4j
public class DataSkewDetector implements IDetector {

    private final DetectorParam param;

    private final DataSkewConfig config;

    public DataSkewDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getDataSkewConfig();
    }

    @Override
    public DetectorResult detect() {

        DetectorResult<List<DataSkewAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.DATA_SKEW.getCategory(), false);

        Map<Integer, SparkJob> jobs = this.param.getReplayEventLogs().getJobs();
        List<DataSkewAbnormal> dataSkewTasks = new ArrayList<>();
        for (SparkJob job : jobs.values()) {
            processingStages(job, dataSkewTasks, detectorResult);
        }

        detectorResult.setData(dataSkewTasks);

        return detectorResult;
    }

    public void processingStages(SparkJob job, List<DataSkewAbnormal> dataSkewTasks, DetectorResult detectorResult) {

        for (SparkStage stage : job.getStages()) {
            DataSkewAbnormal taskDataSkew = new DataSkewAbnormal();
            taskDataSkew.setJobId(job.getJobId());
            taskDataSkew.setStageId(stage.getStageId());
            taskDataSkew.setAbnormal(false);

            if (stage.getSubmissionTimeMap().size() == 0 || stage.getCompleteTimeMap().size() == 0) {
                continue;
            }
            // for stages with retries, the submissionTime and launchTime of different retries should be distinguished.
            for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                Integer attemptId = entry.getKey();
                Long submissionTime = entry.getValue();
                Long completeTime = stage.getCompleteTimeMap().get(attemptId);
                if (submissionTime == null || completeTime == null) {
                    continue;
                }
                List<SparkTask> tasks = stage.getTasksMap().get(attemptId);
                if (tasks == null) {
                    continue;
                }
                List<DataSkewGraph> dataSkewGraphs = new ArrayList<>();
                Long maxShuffleReadBytes = 0L;
                Long firstLaunchTime = 0L;
                double[] totalRecordsList = new double[tasks.size()];
                for (int i = 0; i < tasks.size(); i++) {
                    SparkTask task = tasks.get(i);
                    if (firstLaunchTime == 0 || task.getLaunchTime() < firstLaunchTime) {
                        firstLaunchTime = task.getLaunchTime();
                    }
                    if (task.getTotalShuffleReadBytes() > maxShuffleReadBytes) {
                        maxShuffleReadBytes = task.getTotalShuffleReadBytes();
                    }
                    dataSkewGraphs.add(new DataSkewGraph(task.getTaskId(), task.getTotalShuffleReadBytes(),
                            task.getTotalRecordsRead(), GraphType.normal.toString()));
                    totalRecordsList[i] = task.getTotalRecordsRead();

                }
                if (dataSkewGraphs.size() == 0) {
                    continue;
                }

                if (maxShuffleReadBytes == 0) {
                    continue;
                }
                float stageDuration = completeTime - firstLaunchTime;

                float percentage = (stageDuration / this.param.getAppDuration()) * 100;

                DescriptiveStatistics statistics = new DescriptiveStatistics(totalRecordsList);
                double median = statistics.getPercentile(50);
                double max = statistics.getMax();

                taskDataSkew.setDuration(stageDuration);
                taskDataSkew.setAttemptNumber(attemptId);
                taskDataSkew.setMaxShuffleReadBytes(maxShuffleReadBytes);
                taskDataSkew.setMaxShuffleReadRecords((long) max);
                taskDataSkew.setMedianRecords((long) median);
                taskDataSkew.setRatio(max / median);

                boolean dataSkew = judgeDataSkew(taskDataSkew, max, median, percentage);
                taskDataSkew.setAbnormal(dataSkew);
                if (dataSkew) {
                    detectorResult.setAbnormal(true);
                    dataSkewGraphs.sort(Comparator.comparing(DataSkewGraph::getTotalShuffleReadBytes));
                    // get statistics data
                    Map<Long, DataSkewGraph> statisticsMap = getStatisticsMap(dataSkewGraphs);

                    if (dataSkewGraphs.size() <= 30) {
                        taskDataSkew.setDataSkewGraphs(dataSkewGraphs);
                    } else {
                        taskDataSkew.setDataSkewGraphs(
                                dataSkewGraphs.subList(dataSkewGraphs.size() - 30, dataSkewGraphs.size()));
                    }

                    taskDataSkew.getDataSkewGraphs().forEach(data -> {
                        DataSkewGraph cache = statisticsMap.get(data.getTaskId());
                        if (cache != null) {
                            // type replacement
                            data.setGraphType(cache.getGraphType());
                            statisticsMap.remove(cache.getTaskId());
                        }
                    });
                    statisticsMap.forEach((k, v) -> taskDataSkew.getDataSkewGraphs().add(v));

                }
                dataSkewTasks.add(taskDataSkew);
            }
        }
    }

    /**
     * Data skew judgment
     */
    private boolean judgeDataSkew(DataSkewAbnormal taskDataSkew, double max, double median, float percentage) {
        double threshold = 0;
        if (median > 0) {
            Float multiple = getThreshold(median);
            if (multiple != 0) {
                threshold = multiple * median;
            }
        }
        taskDataSkew.setThreshold(threshold);

        if (threshold == 0) {
            return false;
        }
        // Stage duration / total task duration ratio less than the threshold stage or app duration < configDuration
        if (percentage < this.config.getStageDurationPercentage()
                || this.param.getAppDuration() < this.config.getDuration()) {
            return false;
        }
        return max > threshold;
    }

    /**
     * Get the threshold
     */
    private Float getThreshold(double size) {
        for (MedianInterval interval : this.config.getInterval()) {
            if (interval.getEnd() == 0 && size > interval.getStart()) {
                return interval.getThreshold();
            }
            if (size > interval.getStart() && size <= interval.getEnd()) {
                return interval.getThreshold();
            }
        }
        return 0F;
    }

    /**
     * Statistical value processing
     */
    private Map<Long, DataSkewGraph> getStatisticsMap(List<DataSkewGraph> dataSkewGraphs) {
        Map<Long, DataSkewGraph> statisticsMap = new HashMap<>();
        int middleIndex = dataSkewGraphs.size() / 2;
        if (dataSkewGraphs.size() % 2 == 0) {

            DataSkewGraph pre = dataSkewGraphs.get(middleIndex - 1);
            statisticsMap.put(pre.getTaskId(), new DataSkewGraph(pre.getTaskId(), pre.getTotalShuffleReadBytes(),
                    pre.getTotalRecordsRead(), GraphType.median.toString()));

            DataSkewGraph post = dataSkewGraphs.get(middleIndex);
            statisticsMap.put(post.getTaskId(), new DataSkewGraph(post.getTaskId(), post.getTotalShuffleReadBytes(),
                    post.getTotalRecordsRead(), GraphType.median.toString()));

        } else {
            DataSkewGraph mediaData = dataSkewGraphs.get(middleIndex);
            statisticsMap.put(mediaData.getTaskId(),
                    new DataSkewGraph(mediaData.getTaskId(), mediaData.getTotalShuffleReadBytes(),
                            mediaData.getTotalRecordsRead(), GraphType.median.toString()));
        }
        DataSkewGraph maxGraph = dataSkewGraphs.get(dataSkewGraphs.size() - 1);
        maxGraph.setGraphType(GraphType.max.toString());
        statisticsMap.put(maxGraph.getTaskId(), maxGraph);
        return statisticsMap;
    }
}
