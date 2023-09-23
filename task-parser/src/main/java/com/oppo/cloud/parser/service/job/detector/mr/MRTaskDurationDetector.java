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

package com.oppo.cloud.parser.service.job.detector.mr;

import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.constant.MRTaskType;
import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.GraphType;
import com.oppo.cloud.common.domain.eventlog.TaskDurationGraph;
import com.oppo.cloud.common.domain.mr.MRTaskDurationAbnormal;
import com.oppo.cloud.common.domain.mr.config.MRTaskDurationConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.mr.MRTaskAttemptInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

@Slf4j
public class MRTaskDurationDetector implements IDetector {

    private final DetectorParam param;

    private final MRTaskDurationConfig config;

    public MRTaskDurationDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrTaskDurationConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<MRTaskDurationAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_TASK_DURATION.getCategory(), false);

        List<MRTaskDurationAbnormal> data = new ArrayList<>();
        MRAppInfo mrAppInfo = param.getMrAppInfo();
        MRTaskDurationAbnormal mapResult = judgeTaskDuration(mrAppInfo.getMapList(), MRTaskType.MAP.getName());
        MRTaskDurationAbnormal reduceResult = judgeTaskDuration(mrAppInfo.getReduceList(), MRTaskType.REDUCE.getName());
        if (mapResult.getAbnormal() || reduceResult.getAbnormal()) {
            detectorResult.setAbnormal(true);
            data.add(mapResult);
            data.add(reduceResult);
        }
        detectorResult.setData(data);
        return detectorResult;
    }

    private MRTaskDurationAbnormal judgeTaskDuration(List<MRTaskAttemptInfo> lists, String taskType) {
        MRTaskDurationAbnormal taskDurationAbnormal = new MRTaskDurationAbnormal();
        taskDurationAbnormal.setAbnormal(false);
        taskDurationAbnormal.setTaskType(taskType);

        List<TaskDurationGraph> graphList = new ArrayList<>();

        double[] elapsedTimes = new double[lists.size()];

        for (int i = 0; i < lists.size(); i++) {
            MRTaskAttemptInfo task = lists.get(i);
            graphList.add(new TaskDurationGraph(
                    (long) task.getTaskId(),
                    (double) task.getElapsedTime(),
                    GraphType.normal.toString()));
            elapsedTimes[i] = task.getElapsedTime();
        }

        DescriptiveStatistics statistics = new DescriptiveStatistics(elapsedTimes);
        double median = statistics.getPercentile(50);
        double max = statistics.getMax();
        double ratio = max / median;
        boolean abnormal;
        if (MRTaskType.MAP.getName().equals(taskType)) {
            abnormal = ratio > config.getMapThreshold() && max > config.getTaskDuration();
        } else {
            abnormal = ratio > config.getReduceThreshold() && max > config.getTaskDuration();
        }
        taskDurationAbnormal.setAbnormal(abnormal);
        if (graphList.size() > 0) {
            taskDurationAbnormal.setGraphList(handleGraph(graphList));
        }
        return taskDurationAbnormal;
    }

    private List<TaskDurationGraph> handleGraph(List<TaskDurationGraph> graphList) {
        graphList.sort(Comparator.comparing(TaskDurationGraph::getDuration));
        Map<Long, TaskDurationGraph> statisticsMap = getStatisticsMap(graphList);
        graphList.forEach(data -> {
            TaskDurationGraph cache = statisticsMap.get(data.getTaskId());
            if (cache != null) {
                data.setGraphType(cache.getGraphType());
                statisticsMap.remove(cache.getTaskId());
            }
        });
        statisticsMap.forEach((k, v) -> graphList.add(v));
        return graphList;
    }


    private Map<Long, TaskDurationGraph> getStatisticsMap(List<TaskDurationGraph> graphList) {
        Map<Long, TaskDurationGraph> statisticsMap = new HashMap<>();
        int middleIndex = graphList.size() / 2;
        if (graphList.size() % 2 == 0) {

            TaskDurationGraph pre = graphList.get(middleIndex - 1);
            statisticsMap.put(
                    pre.getTaskId(),
                    new TaskDurationGraph(
                            pre.getTaskId(),
                            pre.getDuration(),
                            GraphType.median.toString()
                    )
            );

            TaskDurationGraph post = graphList.get(middleIndex);
            statisticsMap.put(
                    post.getTaskId(),
                    new TaskDurationGraph(
                            post.getTaskId(),
                            post.getDuration(),
                            GraphType.median.toString()
                    )
            );

        } else {
            TaskDurationGraph mediaData = graphList.get(middleIndex);
            statisticsMap.put(
                    mediaData.getTaskId(),
                    new TaskDurationGraph(
                            mediaData.getTaskId(),
                            mediaData.getDuration(),
                            GraphType.median.toString()
                    )
            );
        }
        TaskDurationGraph maxGraph = graphList.get(graphList.size() - 1);
        maxGraph.setGraphType(GraphType.max.toString());
        statisticsMap.put(maxGraph.getTaskId(), maxGraph);
        return statisticsMap;
    }

}
