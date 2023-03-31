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
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.service.diagnose.Generate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * 运行耗时异常基类
 */
@Slf4j
public abstract class RunTimeBaseService<T extends IsAbnormal> implements Generate {

    @Autowired
    TaskDiagnosisAdviceMapper diagnoseAdviceMapper;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Value(value = "${custom.elasticsearch.detectIndex.name}")
    String detectIndex;

    /**
     * 获取app异常类型(需要子类实现)
     */
    public abstract String getCategory();

    /**
     * 根据es元数据生成分析报告数据(需要子类实现)
     */
    public abstract T generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception;

    /**
     * 分析结论说明（需要各个子类实现）
     */
    public abstract String generateConclusionDesc(Map<String, String> thresholdMap);

    /**
     * 报告描述（需要各个子类实现）
     */
    public abstract String generateItemDesc();

    /**
     * 图表类型
     */
    public abstract String getType();

    @Override
    public Item<T> generate(DetectorStorage detectorStorage) {
        T data = null;
        String error = null;
        Conclusion conclusion = null;
        // es查询元数据
        try {
            for (DetectorResult detectorResult : detectorStorage.getDataList()) {
                if (detectorResult.getAppCategory().equals(this.getCategory())) {
                    // 根据es元数据生成诊断报告
                    data = this.generateData(detectorResult, detectorStorage.getConfig());
                    // 根据诊断结果数据中var变量生成诊断建议
                    conclusion = this.generateConclusion(data);
                    break;
                }
            }

        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            log.error(baos.toString());
            error = baos.toString();
        }
        // 生成建议
        return this.generateItem(data, error, conclusion);
    }

    /**
     * 生成完整的诊断报告数据
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
     * 生成报告结论
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
