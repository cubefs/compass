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

package com.oppo.cloud.syncer.domain;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * mysql bin log 表解析测试
 */
public class TestRawTable {

    @Test
    public void testDecodeTable() {
        String data =
                "{\"data\":[{\"id\":\"583\",\"name\":\"abc\",\"age\":\"11\"}],\"database\":\"test_canal\",\"es\":1644317825000,\"id\":81476,\"isDdl\":false,\"mysqlType\":{\"id\":\"int(11)\",\"name\":\"varchar(32)\",\"age\":\"int(11)\"},\"old\":[{\"age\":\"10\"}],\"pkNames\":[\"id\"],\"sql\":\"\",\"sqlType\":{\"id\":4,\"name\":12,\"age\":4},\"table\":\"foo\",\"ts\":1644317825799,\"type\":\"UPDATE\"}";
        RawTable table = JSON.parseObject(data, RawTable.class);
        Assertions.assertEquals(table.getTable(), "foo");
    }

    @Test
    public void testDecodeTable2() {
        // 修改data=>datax
        String data =
                "{\"datax\":[{\"id\":\"583\",\"name\":\"abc\",\"age\":\"11\"}],\"database\":\"test_canal\",\"es\":1644317825000,\"id\":81476,\"isDdl\":false,\"mysqlType\":{\"id\":\"int(11)\",\"name\":\"varchar(32)\",\"age\":\"int(11)\"},\"old\":[{\"age\":\"10\"}],\"pkNames\":[\"id\"],\"sql\":\"\",\"sqlType\":{\"id\":4,\"name\":12,\"age\":4},\"table\":\"foo\",\"ts\":1644317825799,\"type\":\"UPDATE\"}";
        RawTable table = JSON.parseObject(data, RawTable.class);
        Assertions.assertEquals(table.getData(), null);
    }
}
