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

package com.oppo.cloud.syncer.util;

import com.oppo.cloud.syncer.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试字符串工具
 */
public class TestStringUtil {

    @Test
    public void testReplaceParam() {
        Map<String, String> m = new HashMap<>();
        m.put("id", "12323");
        m.put("name", "Bob");
        m.put("age", "23");

        String s = "The user info is `id=${id},name=${name},age=${age}`";

        String s1 = StringUtil.replaceParams(s, m);
        Assertions.assertEquals(s1, "The user info is `id=12323,name=Bob,age=23`");

        s = "The user info is `id={id},name=${name},age=${age}`";

        s1 = StringUtil.replaceParams(s, m);
        Assertions.assertEquals(s1, "The user info is `id={id},name=Bob,age=23`");
    }
}
