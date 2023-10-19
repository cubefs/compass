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

package com.oppo.cloud.portal.service.diagnose.runtime;

import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.mapper.TaskDiagnosisAdviceMapper;
import com.oppo.cloud.model.TaskDiagnosisAdvice;
import com.oppo.cloud.model.TaskDiagnosisAdviceExample;
import com.oppo.cloud.portal.domain.base.Conclusion;
import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Item;
import com.oppo.cloud.portal.service.OpenSearchService;
import com.oppo.cloud.portal.service.diagnose.Generate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * RunTime base service
 */
@Slf4j
public abstract class RunTimeBaseService<T extends IsAbnormal> implements Generate {

    @Autowired
    TaskDiagnosisAdviceMapper diagnoseAdviceMapper;

    @Autowired
    OpenSearchService openSearchService;

    @Value(value = "${custom.opensearch.detectIndex.name}")
    String detectIndex;

    /**
     * Get app category
     */
    public abstract String getCategory();

    /**
     * Generate data
     */
    public abstract T generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception;

    /**
     * Generate conclusion description
     */
    public abstract String generateConclusionDesc(Map<String, String> thresholdMap);

    /**
     * Generate item description
     */
    public abstract String generateItemDesc();

    /**
     * Get chart type
     */
    public abstract String getType();

    @Override
    public Item<T> generate(DetectorStorage detectorStorage) {
        T data = null;
        String error = null;
        Conclusion conclusion = null;

        try {
            if (detectorStorage.getDataList() != null) {
                for (DetectorResult detectorResult : detectorStorage.getDataList()) {
                    if (detectorResult.getAppCategory().equals(this.getCategory())) {
                        data = this.generateData(detectorResult, detectorStorage.getConfig());
                        conclusion = this.generateConclusion(data);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            log.error(baos.toString());
            error = baos.toString();
        }

        return this.generateItem(data, error, conclusion);
    }

    /**
     * generate item data
     */
    public Item<T> generateItem(T data, String error, Conclusion conclusion) {
        Item<T> res = new Item<>();
        res.setItem(data);
        res.setName(this.generateItemDesc());
        res.setConclusion(conclusion);
        res.setError(error);
        res.setType(this.getType());
        return res;
    }

    /**
     * generate conclusion
     */
    public Conclusion generateConclusion(IsAbnormal isAbnormal) {
        if (isAbnormal == null) {
            return null;
        }
        String advice = "";
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andCategoryEqualTo(this.getCategory());
        List<TaskDiagnosisAdvice> diagnoseAdviceList =
                diagnoseAdviceMapper.selectByExampleWithBLOBs(diagnoseAdviceExample);
        if (diagnoseAdviceList.size() != 0) {
            TaskDiagnosisAdvice diagnoseAdvice = diagnoseAdviceList.get(0);
            advice = diagnoseAdvice.getNormalAdvice();
            try {
                if (isAbnormal.getAbnormal()) {
                    advice = diagnoseAdvice.genAdvice(isAbnormal.getVars());
                } else {
                    advice = diagnoseAdvice.getNormalAdvice();
                }
            } catch (Exception e) {
                log.error("formatAdvice failed, action:{},vars:{}, msg:{}", diagnoseAdvice.getAction(),
                        isAbnormal.getVars(), e.getMessage());
            }
        }

        return new Conclusion(advice, this.generateConclusionDesc(isAbnormal.getVars()));
    }
}
