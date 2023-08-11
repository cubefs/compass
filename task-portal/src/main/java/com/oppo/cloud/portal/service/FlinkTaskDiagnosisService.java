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

import com.oppo.cloud.common.api.CommonPage;
import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskDiagnosis;
import com.oppo.cloud.portal.domain.flink.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface FlinkTaskDiagnosisService {

    CommonStatus<?> pageJobs(DiagnosisAdviceListReq req) throws Exception;

    DiagnosisGeneralViewNumberResp getGeneralViewNumber(@Validated @RequestBody DiagnosisGeneralViewReq request) throws Exception;

    DiagnosisGeneralViewTrendResp getGeneralViewTrend(@Validated @RequestBody DiagnosisGeneralViewReq request) throws Exception;

    DiagnosisGeneralVIewDistributeResp getGeneralViewDistribute(@Validated @RequestBody DiagnosisGeneralViewReq request) throws Exception;

    DiagnosisReportResp getReport(ReportDetailReq request) throws Exception;

    CommonStatus<?> batchMetadata(List<FlinkTaskApp> apps);
}
