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

package com.oppo.cloud.portal.service;

import com.oppo.cloud.portal.domain.diagnose.DiagnoseReport;
import com.oppo.cloud.portal.domain.diagnose.GCReportResp;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.domain.diagnose.oneclick.DiagnoseResult;
import com.oppo.cloud.portal.domain.diagnose.runerror.RunError;
import com.oppo.cloud.portal.domain.task.*;

import java.util.List;
import java.util.Set;

public interface TaskAppService {

    /**
     * 查询任务下的app列表
     */
    TaskAppsResponse searchTaskApps(TaskAppsRequest request) throws Exception;

    /**
     * 生成app的诊断报告
     */
    DiagnoseReport generateReport(String applicationId) throws Exception;


    /**
     * job trend graph
     */
    TrendGraph getGraph(JobsRequest request) throws Exception;

    /**
     * get part of diagnose Report
     */
    List<Item> generatePartOfReport(String applicationId, Set<String> category) throws Exception;


    /**
     * get run info of diagnose report
     */
    DiagnoseReport.RunInfo diagnoseRunInfo(String applicationId) throws Exception;

    /**
     * get run error of diagnose report
     */
    List<Item<RunError>> diagnoseRunError(String applicationId) throws Exception;

    /**
     * get run time of diagnose report
     */
    List<Item> diagnoseRunTime(String applicationIId) throws Exception;

    /**
     * get run resource of diagnose report
     */
    List<Item> diagnoseRunResource(String applicationId) throws Exception;

    /**
     * getGcReport
     */
    GCReportResp getGcReport(String applicationId, String executorId) throws Exception;
}
