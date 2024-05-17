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

package com.oppo.cloud.parser.config;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.common.util.textparser.ParserActionUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Diagnostic rule configuration
 */
@Slf4j
public class DiagnosisConfig {

    /**
     * configuration instance
     */
    private static final DiagnosisConfig INSTANCE = new DiagnosisConfig();

    /**
     * text type diagnostic rule
     */
    public Map<String, String> ruleConfig = new HashMap<>();

    /**
     * spark eventLog diagnostic configuration
     */
    public DetectorConfig detectorConfig;


    public static DiagnosisConfig getInstance() {
        return INSTANCE;
    }

    public void setRuleMap(Map<String, String> ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public void setDetectorConfig(DetectorConfig detectorConfig) {
        this.detectorConfig = detectorConfig;
    }

    public DiagnosisConfig() {

    }

    public List<ParserAction> getActions(String category) {
        List<ParserAction> actions = new ArrayList<>();
        try {
            actions = JSON.parseArray(this.ruleConfig.get(category),ParserAction.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        actions =  ParserActionUtil.verifyParserActions(actions);
        return actions;
    }


}
