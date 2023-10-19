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

import com.oppo.cloud.portal.domain.report.ReportGraph;
import com.oppo.cloud.portal.domain.report.ReportRequest;
import com.oppo.cloud.portal.domain.statistics.StatisticsData;

import java.util.Set;

/**
 * ReportService
 */
public interface ReportService {

    /**
     * Get statistics data
     */
    StatisticsData getStatisticsData(String projectName) throws Exception;

    /**
     * Get the graph chart of the report
     */
    ReportGraph getGraph(ReportRequest reportRequest) throws Exception;

    /**
     * Get projects
     */
    Set<String> getProjects() throws Exception;
}
