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

package com.oppo.cloud.application.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestStringUtil {

    @Test
    public void testReplaceParams() {
        String template = "This is ${name}'s template";
        Map<String, Object> params = new HashMap<>();
        params.put("name", "Bob");

        String result = StringUtil.replaceParams(template, params);
        Assertions.assertEquals(result, "This is Bob's template");
    }

    @Test
    public void testCheckObjectType() {
        Object o = new ArrayList<String>();
        Assertions.assertTrue(o instanceof ArrayList);
        Assertions.assertTrue(o instanceof List);
        Assertions.assertFalse(o instanceof String);

        Map<String, Object> m = new HashMap<>();
        m.put("a", "b");

        Object val = m.get("a");

        List l = new ArrayList<Object>();
        l.add("c");
        l.add(val);
        val = l;
        m.put("a", val);

        Assertions.assertTrue(((List) m.get("a")).size() == 2);

        Object val2 = m.get("a");
        ((List) val2).add("d");
        Assertions.assertTrue(((List) m.get("a")).size() == 3);
    }

    @Test
    public void testEndwith() {
        String s =
                "hdfs://ansible-hdfs:8020/flume/dolphinscheduler/2022-03-18/logs/4839779932416_5/2996/3396.log.1647591885843.tmp";
        System.out.println(s.endsWith(".tmp"));
    }
}
