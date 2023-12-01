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

import java.util.List;
import java.util.Map;

@Data
public class Mask {
    /**
     * Prefix of mask
     */
    private String prefix;
    /**
     * Suffix of mask
     */
    private String suffix;
    /**
     * Mask rules
     */
    private List<MaskRule> rules;

    public Mask(List<MaskRule> rules, String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.rules = rules;
    }

    /**
     * Mask string
     * @param s
     * @return
     */
    public String mask(String s) {
        for (MaskRule rule : this.rules) {
            s = rule.mask(s, prefix, suffix);
        }
        return s;
    }
}
