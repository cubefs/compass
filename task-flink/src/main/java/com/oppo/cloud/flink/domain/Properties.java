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

package com.oppo.cloud.flink.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;

@Data
public class Properties {

    private String key;
    private String value;

    public String getFinalValue(List<Properties> properties) {
        String variableName = findVariableName(value);
        if (variableName == null) {
            return value;
        }

        for (Properties property : properties) {
            if (!property.key.equals(this.key)) {
                continue;
            }

            String variableValue = property.getFinalValue(properties);
            return value.replace("${" + variableName + "}", variableValue);
        }

        return value;
    }

    private String findVariableName(String originalValue) {
        Pattern VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)}"); //变量格式${var_name}
        Matcher matcher = VAR_PATTERN.matcher(originalValue);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public static void main(String[] args) {
        List<Properties> properties = new ArrayList<>();
        Properties prop_1 = new Properties();
        prop_1.key = "yarn.timeline-service.webapp.address";
        prop_1.value = "${yarn.timeline-service.hostname}:8188";

        Properties prop_2 = new Properties();
        prop_2.key = "dfs.webhdfs.user.provider.user.pattern";
        prop_2.value = "^[A-Za-z_][A-Za-z0-9._-]*[$]?$";

        Properties prop_3 = new Properties();
        prop_3.key = "yarn.scheduler.configuration.fs.path";
        prop_3.value = "file://${hadoop.tmp.dir}/yarn/system/schedconf";

        Properties prop_4 = new Properties();
        prop_4.key = "yarn.timeline-service.reader.webapp.address";
        prop_4.value = "${yarn.timeline-service.webapp.address}";

        Properties prop_5 = new Properties();
        prop_5.key = "yarn.timeline-service.hostname";
        prop_5.value = "127.0.0.1";

        properties.add(prop_1);
        properties.add(prop_2);
        properties.add(prop_3);
        properties.add(prop_4);
        properties.add(prop_5);

        assert "127.0.0.1:8188".equals(prop_1.getFinalValue(properties));
        assert "127.0.0.1:8188".equals(prop_4.getFinalValue(properties));
        assert "file://${hadoop.tmp.dir}/yarn/system/schedconf".equals(prop_3.getFinalValue(properties));
        assert "^[A-Za-z_][A-Za-z0-9._-]*[$]?$".equals(prop_2.getFinalValue(properties));
        assert "^127.0.0.1".equals(prop_5.getFinalValue(properties));
    }
}
