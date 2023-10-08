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

package com.oppo.cloud.detect.detector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.constant.TaskStateEnum;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.job.Datum;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.detect.util.DetectorUtil;
import com.oppo.cloud.mapper.TaskDatumMapper;
import com.oppo.cloud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * End time detector.
 */
@Order(2)
@Slf4j
@Service
public class EndTimeAbnormal extends DetectServiceImpl {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskDatumMapper taskDatumMapper;

    @Override
    public void detect(JobAnalysis jobAnalysis) throws Exception {
        // Failed tasks are not subject to runtime duration detection.
        if (jobAnalysis.getTaskState().equals(TaskStateEnum.fail.name())) {
            return;
        }
        double[] normalValue = getEndTimeBaseline(jobAnalysis);
        if (normalValue == null) {
            return;
        }
        double normalEndDateBegin = normalValue[0];
        double normalEndDateEnd = normalValue[1];
        long endTimeTimestamp = jobAnalysis.getEndTime().getTime() / 1000;
        String normalEndDateBeginStr = DetectorUtil.timeStampToStr((long) normalEndDateBegin, "yyyy-MM-dd HH:mm:ss");
        String normalEndDateEndStr = DetectorUtil.timeStampToStr((long) normalEndDateEnd, "yyyy-MM-dd HH:mm:ss");
        jobAnalysis.setEndTimeBaseline(normalEndDateEndStr);
        if (endTimeTimestamp > normalEndDateEnd || endTimeTimestamp < normalEndDateBegin) {
            jobAnalysis.getCategories().add(JobCategoryEnum.endTimeAbnormal.name());
            genBaselineTree(jobAnalysis);
        }
    }

    public void genBaselineTree(JobAnalysis detectJobAnalysis) throws Exception {
        Datum datum = getTotalBaselineTree(detectJobAnalysis);
        String baselineTreeStr = objectMapper.writeValueAsString(datum);
        TaskDatum taskDatum = new TaskDatum();
        taskDatum.setProjectName(detectJobAnalysis.getProjectName());
        taskDatum.setFlowName(detectJobAnalysis.getFlowName());
        taskDatum.setTaskName(detectJobAnalysis.getTaskName());
        taskDatum.setExecutionDate(detectJobAnalysis.getExecutionDate());
        taskDatum.setBaseline(baselineTreeStr);
        taskDatum.setCreateTime(new Date());
        taskDatumMapper.insert(taskDatum);
    }

    /**
     * Get the baseline tree of the upstream task.
     *
     * @param detectJobAnalysis
     * @return
     * @throws Exception
     */
    public Datum getTotalBaselineTree(JobAnalysis detectJobAnalysis) throws Exception {
        Task task = taskService.getTask(detectJobAnalysis.getProjectName(), detectJobAnalysis.getFlowName(),
                detectJobAnalysis.getTaskName());
        Integer taskId = task.getId();
        Datum datum = new Datum();
        Set<Datum.Node> nodeList = new HashSet<>();
        List<Datum.Verge> vergeList = new ArrayList<>();
        Datum.Node node = this.getNode(detectJobAnalysis);
        node.setId(taskId);
        nodeList.add(node);
        datum.setNodeList(nodeList);
        datum.setVergeList(vergeList);
        return datum;
    }

    /**
     * Construct a baseline node.
     *
     * @param detectJobAnalysis
     * @return
     * @throws Exception
     */
    public Datum.Node getNode(JobAnalysis detectJobAnalysis) throws Exception {
        Datum.Node node = new Datum.Node();
        node.setFlowName(detectJobAnalysis.getFlowName());
        node.setTaskName(detectJobAnalysis.getTaskName());
        node.setDuration(detectJobAnalysis.getDuration() == 0 ? "-"
                : DetectorUtil.transferSecond(detectJobAnalysis.getDuration()));
        node.setExecutionDate(DateUtil.format(detectJobAnalysis.getExecutionDate(), "yyyy-MM-dd HH:mm:ss"));
        node.setStartTime(detectJobAnalysis.getStartTime() == null ? "-"
                : DateUtil.format(detectJobAnalysis.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
        node.setEndTime(DateUtil.format(detectJobAnalysis.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
        node.setPeriod(String.format("%s~%s",
                detectJobAnalysis.getStartTime() == null ? "00:00"
                        : DateUtil.format(detectJobAnalysis.getStartTime(), "HH:mm:ss"),
                detectJobAnalysis.getEndTime() == null ? "00:00"
                        : DateUtil.format(detectJobAnalysis.getEndTime(), "HH:mm:ss")));
        node.setTaskState(detectJobAnalysis.getTaskState());
        node.setEndTimeBaseLine("-");
        node.setDurationBaseLine("-");
        if (detectJobAnalysis.getEndTime() != null) {
            double[] normalEndTimeValue = getEndTimeBaseline(detectJobAnalysis);
            if (normalEndTimeValue != null) {
                node.setEndTimeBaseLine(
                        DateUtil.format(new Date((long) (normalEndTimeValue[1]) * 1000L), "yyyy-MM-dd HH:mm:ss"));
                if (detectJobAnalysis.getEndTime().getTime() > normalEndTimeValue[1] * 1000L) {
                    node.setEndTimeAbnormal(true);
                }
            }
        }
        if (detectJobAnalysis.getDuration() != 0) {
            double[] normalDurationValue = getDurationBaseline(detectJobAnalysis);
            if (normalDurationValue != null) {
                node.setDurationBaseLine(DetectorUtil.transferSecond(normalDurationValue[1]));
                if (detectJobAnalysis.getDuration() > normalDurationValue[1]) {
                    node.setDurationAbnormal(true);
                }
            }
        }
        return node;
    }

}
