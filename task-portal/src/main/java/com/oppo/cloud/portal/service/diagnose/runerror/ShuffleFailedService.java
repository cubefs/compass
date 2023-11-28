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
import com.oppo.cloud.portal.util.MessageSourceUtil;
import org.springframework.stereotype.Service;

/**
 * ShuffleFailed Service
 */
@Service
public class ShuffleFailedService extends RunErrorBaseService {

    @Override
    public String getCategory() {
        return AppCategoryEnum.SHUFFLE_FAILED.getCategory();
    }

    @Override
    public String generateItemDesc() {
        return MessageSourceUtil.get("SHUFFLE_FAILED_ANALYSIS");
    }

    @Override
    public Conclusion generateConclusion() {
        return new Conclusion(MessageSourceUtil.get("SHUFFLE_FAILED_CONCLUSION"), MessageSourceUtil.get("SHUFFLE_FAILED_CONCLUSION_DESC"));
    }
}
