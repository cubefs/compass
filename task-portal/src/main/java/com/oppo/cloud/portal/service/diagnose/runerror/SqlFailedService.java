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
 * sql失败
 */
@Service
public class SqlFailedService extends RunErrorBaseService {

    @Override
    public String getCategory() {
        return AppCategoryEnum.SQL_FAILED.getCategory();
    }

    @Override
    public String generateItemDesc() {
        return "sql失败分析";
    }

    @Override
    public Conclusion generateConclusion() {
        String conclusion = "发生语法解析错误，请根据关键日志和对应的诊断建议进行问题修改";
        return new Conclusion(conclusion, "抓取Driver/Executor中语法解析错误的相关日志");
    }
}
