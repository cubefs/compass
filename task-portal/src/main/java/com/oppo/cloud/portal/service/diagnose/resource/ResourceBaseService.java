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

package com.oppo.cloud.portal.service.diagnose.resource;

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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * 资源分析基类
 */
@Slf4j
@Service
public abstract class ResourceBaseService<T extends IsAbnormal> implements Generate {

    @Autowired
    OpenSearchService openSearchService;

    @Autowired
    TaskDiagnosisAdviceMapper diagnoseAdviceMapper;

    @Value(value = "${custom.opensearch.detectIndex.name}")
    String detectIndex;

    /**
     * 获取app异常类型(需要子类实现)
     */
    public abstract String getCategory();

    /**
     * 获取图表类型
     */
    public abstract String getType();

    /**
     * 产生诊断报告数据,由子类实现
     */
    public abstract T generateData(DetectorResult detectorResult, DetectorConfig config,
                                   String applicationId) throws Exception;

    /**
     * 产生分析结论
     */
    public Conclusion generateConclusion(T data) {
        if (data == null) {
            return null;
        }
        String advice = "";
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andCategoryEqualTo(this.getCategory());
        List<TaskDiagnosisAdvice> diagnoseAdviceList =
                diagnoseAdviceMapper.selectByExampleWithBLOBs(diagnoseAdviceExample);
        if (diagnoseAdviceList.size() != 0) {
            TaskDiagnosisAdvice diagnoseAdvice = diagnoseAdviceList.get(0);
            try {
                if (data.getAbnormal()) {
                    advice = diagnoseAdvice.genAdvice(data.getVars());
                } else {
                    advice = diagnoseAdvice.getNormalAdvice();
                }
            } catch (Exception e) {
                log.error("formatAdvice failed, action:{},vars:{}, msg:{}", diagnoseAdvice.getAction(), data.getVars(),
                        e.getMessage());
            }
            return new Conclusion(advice, this.generateConclusionDesc(data));
        }
        return null;
    }

    /**
     * 产生分析结论说明
     */
    public abstract String generateConclusionDesc(IsAbnormal isAbnormal);

    /**
     * 报告描述（需要各个子类实现）
     */
    public abstract String generateItemDesc();

    /**
     * 产生报告模板方法
     */
    @Override
    public Item<T> generate(DetectorStorage detectorStorage) {
        T data = null;
        String error = null;
        Conclusion conclusion = null;
        try {
            if (detectorStorage.getDataList() != null) {
                for (DetectorResult detectorResult : detectorStorage.getDataList()) {
                    if (detectorResult.getAppCategory().equals(this.getCategory())) {
                        // 根据es元数据生成诊断报告
                        data = this.generateData(detectorResult, detectorStorage.getConfig(),
                                detectorStorage.getApplicationId());
                        // 根据诊断结果数据中var变量生成诊断建议
                        conclusion = this.generateConclusion(data);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            error = baos.toString();
        }
        return this.generateItem(data, error, conclusion);
    }

    public Item<T> generateItem(T data, String error, Conclusion conclusion) {
        Item<T> item = new Item<>();
        item.setItem(data);
        item.setName(this.generateItemDesc());
        item.setConclusion(conclusion);
        item.setError(error);
        item.setType(this.getType());
        return item;
    }
}
