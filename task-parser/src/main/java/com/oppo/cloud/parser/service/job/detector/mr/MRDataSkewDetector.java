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
import com.oppo.cloud.common.domain.mr.MRDataSkewAbnormal;
import com.oppo.cloud.common.domain.mr.MRDataSkewGraph;
import com.oppo.cloud.common.domain.mr.config.MRDataSkewConfig;
import com.oppo.cloud.parser.domain.job.MRDetectorParam;
import com.oppo.cloud.parser.domain.mr.CounterInfo;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.mr.MRTaskAttemptInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.*;

public class MRDataSkewDetector implements IDetector {

    private final MRDetectorParam param;

    private final MRDataSkewConfig config;

    MRDataSkewDetector(MRDetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrDataSkewConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<MRDataSkewAbnormal> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_DATA_SKEW.getCategory(), false);

        MRDataSkewAbnormal mrDataSkewAbnormal = new MRDataSkewAbnormal();
        mrDataSkewAbnormal.setIsMapSkew(false);
        mrDataSkewAbnormal.setIsReduceSkew(false);

        MRAppInfo mrAppInfo = param.getMrAppInfo();

        judgeDataSkew(mrDataSkewAbnormal, mrAppInfo.getMapList(), MRTaskType.MAP);
        judgeDataSkew(mrDataSkewAbnormal, mrAppInfo.getReduceList(), MRTaskType.REDUCE);
        if (mrDataSkewAbnormal.getIsMapSkew() || mrDataSkewAbnormal.getIsReduceSkew()) {
            detectorResult.setAbnormal(true);
        }
        detectorResult.setData(mrDataSkewAbnormal);
        return detectorResult;
    }


    private void judgeDataSkew(MRDataSkewAbnormal mrDataSkewAbnormal, List<MRTaskAttemptInfo> lists, MRTaskType taskType) {
        List<MRDataSkewGraph> dataSkewGraphList = new ArrayList<>();

        double[] dataList = new double[lists.size()];
        for (int i = 0; i < lists.size(); i++) {
            MRTaskAttemptInfo task = lists.get(i);
            Long dataSize = 0L;
            switch (taskType) {
                case MAP:
                    String group = CounterInfo.CounterGroupName.FILE_SYSTEM_COUNTER.getCounterGroupName();
                    Map<String, Long> metrics = task.getCounters().get(group);
                    dataSize = metrics.get(CounterInfo.CounterName.HDFS_BYTES_READ.getCounterName());
                    break;
                case REDUCE:
                    group = CounterInfo.CounterGroupName.TASK_COUNTER.getCounterGroupName();
                    metrics = task.getCounters().get(group);
                    dataSize = metrics.get(CounterInfo.CounterName.REDUCE_SHUFFLE_BYTES.getCounterName());
                    break;
                default:
                    break;

            }
            dataList[i] = dataSize;
            dataSkewGraphList.add(new MRDataSkewGraph(
                    (long) task.getTaskId(),
                    dataSize,
                    GraphType.normal.toString())
            );
        }


        DescriptiveStatistics statistics = new DescriptiveStatistics(dataList);
        double median = statistics.getPercentile(50);
        double max = statistics.getMax();
        double ratio = max / median;

        boolean skew;
        switch (taskType) {
            case MAP:
                skew = ratio > config.getMapThreshold();
                if (skew) {
                    mrDataSkewAbnormal.setIsMapSkew(true);
                    mrDataSkewAbnormal.setMapRatio(ratio);
                    mrDataSkewAbnormal.setMapGraphList(handleSkewGraph(dataSkewGraphList));
                }
                break;
            case REDUCE:
                skew = ratio > config.getReduceThreshold();
                if (skew) {
                    mrDataSkewAbnormal.setIsReduceSkew(true);
                    mrDataSkewAbnormal.setReduceRatio(ratio);
                    mrDataSkewAbnormal.setReduceGraphList(handleSkewGraph(dataSkewGraphList));
                }
                break;
            default:
                break;
        }
    }

    private List<MRDataSkewGraph> handleSkewGraph(List<MRDataSkewGraph> dataSkewGraphList) {
        dataSkewGraphList.sort(Comparator.comparing(MRDataSkewGraph::getDataSize));
        Map<Long, MRDataSkewGraph> statisticsMap = getStatisticsMap(dataSkewGraphList);
        dataSkewGraphList.forEach(data -> {
            MRDataSkewGraph cache = statisticsMap.get(data.getTaskId());
            if (cache != null) {
                data.setGraphType(cache.getGraphType());
                statisticsMap.remove(cache.getTaskId());
            }
        });
        statisticsMap.forEach((k, v) -> dataSkewGraphList.add(v));
        return dataSkewGraphList;
    }

    private Map<Long, MRDataSkewGraph> getStatisticsMap(List<MRDataSkewGraph> dataSkewGraphs) {
        Map<Long, MRDataSkewGraph> statisticsMap = new HashMap<>();
        int middleIndex = dataSkewGraphs.size() / 2;
        if (dataSkewGraphs.size() % 2 == 0) {

            MRDataSkewGraph pre = dataSkewGraphs.get(middleIndex - 1);
            statisticsMap.put(
                    pre.getTaskId(),
                    new MRDataSkewGraph(
                            pre.getTaskId(),
                            pre.getDataSize(),
                            GraphType.median.toString()
                    )
            );

            MRDataSkewGraph post = dataSkewGraphs.get(middleIndex);
            statisticsMap.put(
                    post.getTaskId(),
                    new MRDataSkewGraph(
                            post.getTaskId(),
                            post.getDataSize(),
                            GraphType.median.toString()
                    )
            );

        } else {
            MRDataSkewGraph mediaData = dataSkewGraphs.get(middleIndex);
            statisticsMap.put(
                    mediaData.getTaskId(),
                    new MRDataSkewGraph(
                            mediaData.getTaskId(),
                            mediaData.getDataSize(),
                            GraphType.median.toString()
                    )
            );
        }
        MRDataSkewGraph maxGraph = dataSkewGraphs.get(dataSkewGraphs.size() - 1);
        maxGraph.setGraphType(GraphType.max.toString());
        statisticsMap.put(maxGraph.getTaskId(), maxGraph);
        return statisticsMap;
    }

}
