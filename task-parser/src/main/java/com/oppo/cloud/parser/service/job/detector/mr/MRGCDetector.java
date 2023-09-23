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
import com.oppo.cloud.common.domain.mr.MRGCAbnormal;
import com.oppo.cloud.common.domain.mr.MRGCGraph;
import com.oppo.cloud.common.domain.mr.config.MRGCConfig;
import com.oppo.cloud.parser.domain.job.DetectorParam;
import com.oppo.cloud.parser.domain.mr.CounterInfo;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.mr.MRTaskAttemptInfo;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MRGCDetector implements IDetector {

    private final DetectorParam param;

    private final MRGCConfig config;

    public MRGCDetector(DetectorParam param) {
        this.param = param;
        this.config = param.getConfig().getMrGCConfig();
    }

    @Override
    public DetectorResult detect() {
        DetectorResult<List<MRGCAbnormal>> detectorResult =
                new DetectorResult<>(AppCategoryEnum.MR_GC_ABNORMAL.getCategory(), false);

        MRAppInfo mrAppInfo = param.getMrAppInfo();
        MRGCAbnormal mapGCInfo = judgeGCAbnormal(mrAppInfo.getMapList(), MRTaskType.MAP);
        MRGCAbnormal reduceGCInfo = judgeGCAbnormal(mrAppInfo.getReduceList(), MRTaskType.REDUCE);
        List<MRGCAbnormal> data = new ArrayList<>();
        if (mapGCInfo.getAbnormal() || reduceGCInfo.getAbnormal()) {
            detectorResult.setAbnormal(true);
            data.add(mapGCInfo);
            data.add(reduceGCInfo);
        }
        detectorResult.setData(data);
        return detectorResult;
    }

    private MRGCAbnormal judgeGCAbnormal(List<MRTaskAttemptInfo> lists, MRTaskType taskType) {
        MRGCAbnormal gcInfo = new MRGCAbnormal();
        List<MRGCGraph> graphList = new ArrayList<>();

        double[] cpuTimes = new double[lists.size()];
        double[] gcTimes = new double[lists.size()];

        for (int i = 0; i < lists.size(); i++) {
            MRTaskAttemptInfo task = lists.get(i);
            String group = CounterInfo.CounterGroupName.TASK_COUNTER.getCounterGroupName();
            Map<String, Long> metrics = task.getCounters().get(group);
            long cpuMs = metrics.getOrDefault(CounterInfo.CounterName.CPU_MILLISECONDS.getCounterName(), 0L);
            long gcMs = metrics.getOrDefault(CounterInfo.CounterName.GC_TIME_MILLIS.getCounterName(), 0L);
            graphList.add(new MRGCGraph(
                    (long) task.getTaskId(),
                    (double) cpuMs,
                    (double) gcMs,
                    GraphType.normal.toString()));
            cpuTimes[i] = cpuMs;
            gcTimes[i] = gcMs;
        }

        DescriptiveStatistics cpuTimeStatistics = new DescriptiveStatistics(cpuTimes);
        double cpuTimeMedian = cpuTimeStatistics.getMean();

        DescriptiveStatistics gcTimeStatistics = new DescriptiveStatistics(gcTimes);
        double gcTimeMedian = gcTimeStatistics.getMean();

        double ratio = gcTimeMedian / cpuTimeMedian;

        boolean abnormal = false;
        switch (taskType) {
            case MAP:
                abnormal = ratio > config.getMapThreshold();
                break;
            case REDUCE:
                abnormal = ratio > config.getReduceThreshold();
                break;
            default:
                break;
        }
        gcInfo.setAbnormal(abnormal);
        gcInfo.setCpuTimeMedian(cpuTimeMedian);
        gcInfo.setGcTimeMedian(gcTimeMedian);
        gcInfo.setRatio(ratio);
        gcInfo.setTaskType(taskType.getName());
        gcInfo.setGraphList(graphList);
        return gcInfo;

    }
}
