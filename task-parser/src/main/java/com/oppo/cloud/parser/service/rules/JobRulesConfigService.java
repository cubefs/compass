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

package com.oppo.cloud.parser.service.rules;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.common.util.textparser.ParserActionUtil;
import com.oppo.cloud.parser.config.CustomConfig;
import com.oppo.cloud.parser.config.DiagnosisConfig;
import com.oppo.cloud.parser.domain.rule.Rules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JobRulesConfigService {
    @Autowired
    private CustomConfig customConfig;

    public DetectorConfig detectorConfig;

    private Map<String, String> rulesConfig;

    @PostConstruct
    private void init() {
        readConfig();
        DiagnosisConfig.getInstance().setDetectorConfig(this.detectorConfig);
        DiagnosisConfig.getInstance().setRuleMap(this.rulesConfig);
    }

    public void readConfig() {
        Map<String, String> rulesMap = new HashMap<>();
        InputStream input = JobRulesConfigService.class.getClassLoader().getResourceAsStream("rules.json");
        if (input == null) {
            return;
        }
        List<Rules> rules = JSON.parseArray(JSON.parseArray(input).toString(), Rules.class);

        for (Rules rule : rules) {
            rulesMap.put(rule.getLogType(), JSON.toJSONString(rule.getActions()));
        }

        this.detectorConfig = this.customConfig.eventLogDetectorConfig();
        this.rulesConfig = rulesMap;
    }

}
