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

package com.oppo.cloud.parser.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.domain.cluster.hadoop.NameNodeConf;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.parser.domain.rule.Rules;
import org.junit.jupiter.api.BeforeAll;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserConfigLoader {

    private static Map<String, String> rulesConfig;
    private static DetectorConfig detectorConfig;
    private static Map<String, NameNodeConf> nameNodeConfMap;

    @BeforeAll
    public static void init() {
        if (rulesConfig == null) {
            rulesConfig = readRulesConfig();
        }
        if (detectorConfig == null) {
            detectorConfig = readDetectorConfig();
        }
        if (nameNodeConfMap == null) {
            nameNodeConfMap = readNameNodeConf();
        }
    }
    public static Map<String, String> getRulesConfig() {
        return rulesConfig;
    }

    public static DetectorConfig getDetectorConfig() {
        return detectorConfig;
    }

    public static Map<String, NameNodeConf> getNameNodeConfMap() {
        return nameNodeConfMap;
    }


    private static Map<String, String> readRulesConfig() {
        HashMap<String, String> rulesMap = new HashMap<>();
        InputStream resource = ParserConfigLoader.class.getClassLoader().getResourceAsStream("rules.json");
        if (resource == null) {
            return null;
        }
        List<Rules> rules = JSON.parseArray(JSON.parseArray(resource).toString(), Rules.class);
        for (Rules rule: rules) {
            rulesMap.put(rule.getLogType(), JSON.toJSONString(rule.getActions()));
        }
        return rulesMap;
    }

    private static DetectorConfig readDetectorConfig() {
        try {
            final Yaml yaml = new Yaml();
            final Map<String, Object> confMap = yaml.load(createFileReader("application.yml"));
            JSONObject detectorConfigJSON = JSONObject.parseObject(JSON.toJSONString(confMap))
                    .getJSONObject("custom")
                    .getJSONObject("detector");
            return JSONObject.parseObject(detectorConfigJSON.toString(), DetectorConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, NameNodeConf> readNameNodeConf() {
        try {
            final Yaml yaml = new Yaml();
            final Map<String, Object> confMap = yaml.load(createFileReader("application-hadoop.yml"));
            JSONArray nameNodeConfs = JSONObject.parseObject(JSON.toJSONString(confMap))
                    .getJSONObject("hadoop")
                    .getJSONArray("namenodes");
            final Map<String, NameNodeConf> nameNodeConfMap = new HashMap<>();
            for (int i = 0; i < nameNodeConfs.size(); i++) {
                JSONObject conf = nameNodeConfs.getJSONObject(i);
                NameNodeConf nameNodeConf = JSONObject.parseObject(conf.toString(), NameNodeConf.class);
                nameNodeConfMap.put(nameNodeConf.getNameservices(), nameNodeConf);
            }
            return nameNodeConfMap;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileReader createFileReader(String fileName) throws FileNotFoundException {
        String filePath = ParserConfigLoader.class.getClassLoader().getResource(fileName).getPath();
        return new FileReader(filePath);
    }
}
