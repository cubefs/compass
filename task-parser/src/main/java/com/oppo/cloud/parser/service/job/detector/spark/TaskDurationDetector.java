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
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.GraphType;
import com.oppo.cloud.common.domain.eventlog.TaskDurationAbnormal;
import com.oppo.cloud.common.domain.eventlog.TaskDurationGraph;
import com.oppo.cloud.common.domain.eventlog.config.TaskDurationConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkJob;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkStage;
import com.oppo.cloud.parser.domain.spark.eventlog.SparkTask;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

@Slf4j
public class TaskDurationDetector implements IDetector {

    private final DetectorParam param;

    private final TaskDurationConfig config;

    public TaskDurationDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getTaskDurationConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<TaskDurationAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.TASK_DURATION.getCategory(), false);

        List<TaskDurationAbnormal> taskDurationAbnormalList = new ArrayList<>();
        for (Map.Entry<Integer, SparkJob> job : param.getReplayEventLogs().getJobs().entrySet()) {
            List<SparkStage> stages = job.getValue().getStages();
            for (SparkStage stage : stages) {
                for (Map.Entry<Integer, Long> entry : stage.getSubmissionTimeMap().entrySet()) {
                    Integer attemptId = entry.getKey();
                    if (stage.getFailed().get(attemptId) != null) {
                        continue;
                    }
                    List<SparkTask> tasks = stage.getTasksMap().get(attemptId);
                    double[] taskDurationList = new double[tasks.size()];
                    List<TaskDurationGraph> taskDurationGraphList = new ArrayList<>();
                    for (int i = 0; i < tasks.size(); i++) {
                        SparkTask task = tasks.get(i);
                        double duration = (double) (task.getFinishTime() - task.getLaunchTime());
                        taskDurationList[i] = duration;
                        taskDurationGraphList.add(new TaskDurationGraph(
                                task.getTaskId(),
                                duration,
                                GraphType.normal.toString()));
                    }
                    Arrays.sort(taskDurationList);
                    taskDurationGraphList.sort(Comparator.comparing(TaskDurationGraph::getDuration));

                    DescriptiveStatistics statistics = new DescriptiveStatistics(taskDurationList);
                    double median = statistics.getPercentile(50);
                    double max = statistics.getMax();

                    double threshold = this.config.getThreshold();
                    Long duration = this.config.getDuration();

                    double ratio = max / median;

                    TaskDurationAbnormal taskDurationAbnormal = new TaskDurationAbnormal(job.getKey(),
                            stage.getStageId(), attemptId, median, max, ratio, null, false);
                    // 阈值判断
                    if (ratio > threshold && this.param.getAppDuration() > duration) {
                        // 统计值处理
                        Map<Long, TaskDurationGraph> statisticsMap = getStatisticsMap(taskDurationGraphList);
                        List<TaskDurationGraph> graphs;
                        if (taskDurationGraphList.size() <= 30) {
                            graphs = taskDurationGraphList;
                        } else {
                            graphs = taskDurationGraphList.subList(taskDurationGraphList.size() - 30,
                                    taskDurationGraphList.size());
                        }
                        taskDurationAbnormal.setAbnormal(true);
                        graphs.forEach(data -> {
                            TaskDurationGraph cache = statisticsMap.get(data.getTaskId());
                            {
                                if (cache != null) {
                                    // 类型替换
                                    data.setGraphType(cache.getGraphType());
                                    statisticsMap.remove(cache.getTaskId());
                                }
                            }
                        });
                        statisticsMap.forEach((k, v) -> graphs.add(v));
                        taskDurationAbnormal.setGraphs(graphs);
                        detectorResult.setAbnormal(true);
                    }
                    taskDurationAbnormalList.add(taskDurationAbnormal);
                }
            }
        }
        if (taskDurationAbnormalList.size() == 0) {
            return null;
        }
        detectorResult.setData(taskDurationAbnormalList);
        return detectorResult;
    }

    /**
     * 统计值处理
     */
    private Map<Long, TaskDurationGraph> getStatisticsMap(List<TaskDurationGraph> taskDurationGraphs) {
        Map<Long, TaskDurationGraph> statisticsMap = new HashMap<>();
        int middleIndex = taskDurationGraphs.size() / 2;
        if (taskDurationGraphs.size() % 2 == 0) {

            TaskDurationGraph pre = taskDurationGraphs.get(middleIndex - 1);
            statisticsMap.put(
                    pre.getTaskId(),
                    new TaskDurationGraph(
                            pre.getTaskId(),
                            pre.getDuration(),
                            GraphType.median.toString()));

            TaskDurationGraph post = taskDurationGraphs.get(middleIndex);
            statisticsMap.put(
                    post.getTaskId(),
                    new TaskDurationGraph(
                            post.getTaskId(),
                            post.getDuration(),
                            GraphType.median.toString()));

        } else {
            TaskDurationGraph mediaData = taskDurationGraphs.get(middleIndex);
            statisticsMap.put(
                    mediaData.getTaskId(),
                    new TaskDurationGraph(
                            mediaData.getTaskId(),
                            mediaData.getDuration(),
                            GraphType.median.toString()));
        }
        TaskDurationGraph maxGraph = taskDurationGraphs.get(taskDurationGraphs.size() - 1);
        maxGraph.setGraphType(GraphType.max.toString());
        statisticsMap.put(maxGraph.getTaskId(), maxGraph);
        return statisticsMap;
    }

}
