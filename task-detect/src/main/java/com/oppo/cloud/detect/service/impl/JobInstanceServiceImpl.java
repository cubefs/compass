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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.common.domain.opensearch.JobAnalysis;
import com.oppo.cloud.common.domain.opensearch.JobInstance;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.detect.service.OpenSearchService;
import com.oppo.cloud.detect.service.JobInstanceService;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务实例处理
 */

@Service
public class JobInstanceServiceImpl implements JobInstanceService {

    @Autowired
    private OpenSearchService openSearchService;

    @Value("${custom.opensearch.job-instance-index}")
    private String jonInstanceIndex;

    @Override
    public void insertOrUpdate(JobAnalysis jobAnalysis) throws Exception {
        JobInstance jobInstance = new JobInstance();
        BeanUtils.copyProperties(jobAnalysis, jobInstance);
        jobInstance.setIndex(null);
        jobInstance.setDocId(null);
        Map<String, Object> termQuery = new HashMap<>();
        termQuery.put("projectName.keyword", jobAnalysis.getProjectName());
        termQuery.put("flowName.keyword", jobAnalysis.getFlowName());
        termQuery.put("taskName.keyword", jobAnalysis.getTaskName());
        termQuery.put("executionDate", DateUtil.timestampToUTCDate(jobAnalysis.getExecutionDate().getTime()));
        SearchSourceBuilder searchSourceBuilder = openSearchService.genSearchBuilder(termQuery, null, null, null);
        List<JobInstance> jobInstanceList =
                openSearchService.find(JobInstance.class, searchSourceBuilder, jonInstanceIndex + "-*");
        if (jobInstanceList.size() != 0) {
            JobInstance jobInstanceEs = jobInstanceList.get(0);
            jobInstance.setDocId(jobInstanceEs.getDocId());
            jobInstance.setIndex(jobInstanceEs.getIndex());
        }
        openSearchService.insertOrUpDate(jobInstance.genIndex(jonInstanceIndex), jobInstance.genDocId(),
                jobInstance.genDoc());
    }
}
