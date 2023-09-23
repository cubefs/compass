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

import com.oppo.cloud.common.constant.JobCategoryEnum;
import com.oppo.cloud.common.constant.TaskStateEnum;
import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.detect.domain.TaskStateHistory;
import com.oppo.cloud.detect.service.TaskInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 长期失败检测器
 */
@Order(3)
@Service
@Slf4j
public class AlwaysFailed extends DetectServiceImpl {

    @Value("${custom.detectionRule.alwaysFailedWarning}")
    private Integer alwaysFailedWarning;

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Override
    public void detect(JobAnalysis jobAnalysis) throws Exception {
        if (jobAnalysis.getTaskState().equals(TaskStateEnum.success.name())) {
            return;
        }
        Date endExecutionTime = DateUtil.getOffsetDate(jobAnalysis.getExecutionDate(), -60);
        // 查询该任务最近两个月的执行情况
        List<TaskStateHistory> taskStateHistories =
                taskInstanceService.searchTaskStateHistory(jobAnalysis.getProjectName(),
                        jobAnalysis.getFlowName(), jobAnalysis.getTaskName(), jobAnalysis.getExecutionDate(),
                        endExecutionTime, 60);
        log.debug("taskName:{}, executionTime:{}, taskStateHistories:{}", jobAnalysis.getTaskName(),
                jobAnalysis.getEndTime(), taskStateHistories);
        if (taskStateHistories == null || taskStateHistories.size() == 0) {
            return;
        }
        Date lastSuccessDate = DateUtil.getOffsetDate(jobAnalysis.getExecutionDate(), -alwaysFailedWarning);
        if (taskStateHistories.get(taskStateHistories.size() - 1).getExecutionTime().getTime() > lastSuccessDate
                .getTime()) {
            // 未满足执行10天的任务直接过滤掉
            return;
        }
        long recentSuccess = 0L;
        for (TaskStateHistory taskStateHistory : taskStateHistories) {
            long executionTime = taskStateHistory.getExecutionTime().getTime() / 1000;
            String state = taskStateHistory.getState();
            // 在指定时间内有成功任务
            if (state.equals(TaskStateEnum.success.name())
                    && ((executionTime > jobAnalysis.getExecutionDate().getTime() / 1000
                            - alwaysFailedWarning * 24 * 3600))) {
                return;
            }
            // 在时间范围内记录最近一次成功的时间
            if (state.equals(TaskStateEnum.success.name())) {
                recentSuccess = executionTime;
                break;
            }
        }
        if (recentSuccess != 0L) {
            jobAnalysis.setSuccessExecutionDay(DateUtil.format(new Date(recentSuccess * 1000)));
            jobAnalysis.setSuccessDays(
                    String.valueOf((jobAnalysis.getExecutionDate().getTime() / 1000 - recentSuccess) / (24 * 3600)));
        }
        jobAnalysis.getCategories().add(JobCategoryEnum.alwaysFailed.name());
    }
}
