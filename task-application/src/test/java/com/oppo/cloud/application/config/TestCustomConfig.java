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

package com.oppo.cloud.application.config;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.application.domain.LogPathJoin;
import com.oppo.cloud.application.domain.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootConfiguration
@SpringBootTest
@ComponentScan(basePackages = "com.oppo.cloud")
public class TestCustomConfig {

    @Autowired
    private CustomConfig customConfig;

    @Autowired
    private HadoopConfig hadoopConfig;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    public void testParseNamenodeConfig() {
        Assertions.assertTrue(hadoopConfig.getNamenodes() != null, "namenode config should not be null");
        Assertions.assertTrue(hadoopConfig.getNamenodes().size() > 0, "namenode config should not be empty");
    }

    @Test
    public void testParseRuleConfig() {
        Assertions.assertTrue(customConfig.getRules() != null, "rules should not be null");
        Assertions.assertTrue(customConfig.getRules().size() > 0, "rules should not be empty");
    }

    @Test
    public void testParseRuleLog() {
        String path = "/data1_1T/dolphinscheduler/logs/4563211890432_1/271/284.log";
        String regex = "^.*(?<logpath>logs/.*)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);

        Assertions.assertTrue(matcher.matches(), "path should be matched");
    }

    @Test
    public void testParseRuleLog2() {
        String dateString = "2022-02-18 01:43:11";
        String regex = "^.*(?<date>\\d{4}-\\d{2}-\\d{2}).+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dateString);
        Assertions.assertTrue(matcher.matches(), "date should be matched");
        Assertions.assertEquals(matcher.group("date"), "2022-02-18");
    }

    @Disabled
    @Test
    public void testParseRuleLog3() {
        Map<String, Object> m = jdbcTemplate.queryForMap("select * from t_ds_task_instance where id=284");
        System.out.println(m);

        List<String> paths = new ArrayList<>();
        for (Rule rule : customConfig.getRules()) {
            for (LogPathJoin logPathJoin : rule.getLogPathJoins()) {
                if (logPathJoin.getColumn() == null || logPathJoin.getColumn().isEmpty()) {
                    paths.add(logPathJoin.getData());
                } else {
                    String columnData = m.get(logPathJoin.getColumn()).toString();
                    Pattern pattern = Pattern.compile(logPathJoin.getRegex());
                    Matcher matcher = pattern.matcher(columnData);
                    if (matcher.matches()) {
                        String matchedData = matcher.group(logPathJoin.getName());
                        paths.add(matchedData);
                    }
                }
            }
            break;
        }
        System.out.println(paths);
        String path = String.join("/", paths);
        System.out.println(path);
    }

    @Test
    public void testParseString2Map() {
        String str = "{\"age\": 24, \"name\": \"Bob\"}";
        Map<String, Object> m = JSON.parseObject(str);
        System.out.println(m);
    }

    @Test
    public void testParseMatchTaskId() {
        String message =
                "[INFO] 2022-02-22 11:02:27.417  - [taskAppId=TASK-4574789155328_1-2413-2607]:[90] - execId: exec_id018013linkis-cg-entrancebjht3929:9104IDE_hdfs_spark_4, taskId: 2859";
        String regex = "^\\[INFO\\].+execId.+taskId:\\s*(?<taskId>\\d+).*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        Assertions.assertTrue(matcher.matches(), "taskId should be matched");
        Assertions.assertEquals(matcher.group("taskId"), "2859");
    }

    @Test
    public void testParseMatchApplicationId() {
        String message = "2022-02-22 11:02:28.002 INFO yarn application id: application_1642582961937_0382";
        String regex = "^.*(?<applicationId>application_[0-9]+_[0-9]+).*$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(message);
        Assertions.assertTrue(matcher.matches(), "applicationId should be matched");
        Assertions.assertEquals(matcher.group("applicationId"), "application_1642582961937_0382");
    }

    @Test
    public void testConvertDataType() {
        Map<String, Object> m = new HashMap<>();
        String a = (String) m.get("a");
        System.out.println(a);
    }
}
