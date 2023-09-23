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
import com.oppo.cloud.common.domain.eventlog.*;
import com.oppo.cloud.common.domain.eventlog.config.HdfsStuckConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class HdfsStuckDetector implements IDetector {

    private final DetectorParam param;

    private final HdfsStuckConfig config;

    public HdfsStuckDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getHdfsStuckConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<HdfsStuckAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.HDFS_STUCK.getCategory(), false);

        List<HdfsStuckAbnormal> hdfsStuckAbnormalList = new ArrayList<>();
        for (Map.Entry<Integer, SparkJob> job : this.param.getReplayEventLogs().getJobs().entrySet()) {
            List<SparkStage> stages = job.getValue().getStages();
            for (SparkStage stage : stages) {
                for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                    Integer attemptId = entry.getKey();
                    if (stage.getFailed().get(attemptId) != null) {
                        continue;
                    }
                    List<SparkTask> tasks = stage.getTasksMap().get(attemptId);
                    List<HdfsStuckGraph> hdfsStuckGraphs = new ArrayList<>();
                    for (SparkTask task : tasks) {
                        if (task.getBytesRead() == 0) {
                            continue;
                        }
                        double duration = (double) (task.getFinishTime() - task.getLaunchTime());
                        // task读取数据量(MB)/耗时分布(s)
                        double taskSize = task.getBytesRead() / 1024.0 / 1024.0;

                        if (taskSize < this.config.getTaskSize() || duration < this.config.getDuration()) {
                            continue;
                        }
                        double percent = taskSize / (duration / 1000);
                        hdfsStuckGraphs.add(new HdfsStuckGraph(task.getTaskId(), percent, GraphType.normal.toString()));
                    }
                    if (hdfsStuckGraphs.size() == 0) {
                        continue;
                    }

                    judge(hdfsStuckGraphs, hdfsStuckAbnormalList, detectorResult, job.getKey(), stage.getStageId(),
                            attemptId);

                }
            }
        }

        if (hdfsStuckAbnormalList.size() == 0) {
            return null;
        }
        detectorResult.setData(hdfsStuckAbnormalList);
        return detectorResult;
    }

    // 卡顿判断
    private void judge(List<HdfsStuckGraph> hdfsSlowGraphList,
                       List<HdfsStuckAbnormal> hdfsSlowAbnormalList,
                       DetectorResult detectorResult,
                       Integer jobId,
                       Integer stageId,
                       Integer attemptId) {

        hdfsSlowGraphList.sort(Comparator.comparing(HdfsStuckGraph::getPercent));

        double median;
        int middleIndex = hdfsSlowGraphList.size() / 2;

        Map<Long, HdfsStuckGraph> statisticsMap = new HashMap<>();
        if (hdfsSlowGraphList.size() % 2 == 0) {

            HdfsStuckGraph pre = hdfsSlowGraphList.get(middleIndex - 1);
            statisticsMap.put(
                    pre.getTaskId(),
                    new HdfsStuckGraph(pre.getTaskId(), pre.getPercent(), GraphType.median.toString()));

            HdfsStuckGraph post = hdfsSlowGraphList.get(middleIndex);
            statisticsMap.put(
                    post.getTaskId(),
                    new HdfsStuckGraph(post.getTaskId(), post.getPercent(), GraphType.median.toString()));

            median = (pre.getPercent() + post.getPercent()) / 2;
        } else {
            HdfsStuckGraph mediaData = hdfsSlowGraphList.get(middleIndex);
            statisticsMap.put(
                    mediaData.getTaskId(),
                    new HdfsStuckGraph(mediaData.getTaskId(), mediaData.getPercent(), GraphType.median.toString()));
            median = mediaData.getPercent();
        }

        HdfsStuckGraph minGraph = hdfsSlowGraphList.get(0);
        minGraph.setGraphType(GraphType.min.toString());
        double min = minGraph.getPercent();
        statisticsMap.put(minGraph.getTaskId(), minGraph);

        double ratio = 0.0;
        if (min != 0) {
            ratio = median / min;
        }
        double threshold = this.config.getThreshold();
        if (ratio > threshold) {
            List<HdfsStuckGraph> graphs;
            if (hdfsSlowGraphList.size() <= 30) {
                graphs = hdfsSlowGraphList;
            } else {
                graphs = hdfsSlowGraphList.subList(hdfsSlowGraphList.size() - 30,
                        hdfsSlowGraphList.size());
            }
            // 统计类型处理
            graphs.forEach(data -> {
                HdfsStuckGraph cache = statisticsMap.get(data.getTaskId());
                if (cache != null) {
                    // 类型替换
                    data.setGraphType(cache.getGraphType());
                    statisticsMap.remove(cache.getTaskId());
                }
            });

            statisticsMap.forEach((k, v) -> graphs.add(v));

            hdfsSlowAbnormalList.add(
                    new HdfsStuckAbnormal(jobId, stageId, attemptId, ratio, median, min, threshold, graphs, true));

            detectorResult.setAbnormal(true);

        } else {
            hdfsSlowAbnormalList.add(
                    new HdfsStuckAbnormal(jobId, stageId, attemptId, ratio, median, min, threshold, null, false));
        }
    }
}
