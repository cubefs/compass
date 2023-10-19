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

package com.oppo.cloud.portal.service.diagnose.runerror;

import com.oppo.cloud.common.constant.AppCategoryEnum;

import com.oppo.cloud.portal.domain.base.Conclusion;
import org.springframework.stereotype.Service;

/**
 * Memory Overflow Service
 */
@Service
public class MemoryOverflowService extends RunErrorBaseService {

    @Override
    public String getCategory() {
        return AppCategoryEnum.MEMORY_OVERFLOW.getCategory();

    }

    @Override
    public String generateItemDesc() {
        return "内存溢出分析";
    }

    @Override
    public Conclusion generateConclusion() {
        String conclusion = "运行过程发生内存溢出,请根据关键日志和相应的诊断建议进行问题修复";
        return new Conclusion(conclusion, "抓取Driver/Executor中内存溢出的相关日志");
    }

}
