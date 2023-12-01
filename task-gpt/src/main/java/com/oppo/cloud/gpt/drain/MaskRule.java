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

package com.oppo.cloud.gpt.drain;

import lombok.Data;

@Data
public class MaskRule {
    /**
     * Replacement for the regex.
     */
    private String maskWith;
    /**
     * Express for matching.
     */
    private String regex;

    public MaskRule() {
    }

    public MaskRule(String regex, String maskWith) {
        this.regex = regex;
        this.maskWith = maskWith;
    }

    /**
     * Replace string with mask according to regex.
     *
     * @param s
     * @param prefix
     * @param suffix
     * @return
     */
    public String mask(String s, String prefix, String suffix) {
        return s.replaceAll(regex, prefix + maskWith + suffix);
    }
}
