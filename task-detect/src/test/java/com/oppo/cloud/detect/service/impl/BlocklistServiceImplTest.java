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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.detect.service.BlocklistService;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.model.Blocklist;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class BlocklistServiceImplTest {

    @MockBean(name = "blocklistMapper")
    BlocklistMapper blocklistMapper;

    @SpyBean
    BlocklistService blocklistService;

    @Test
    void isBlocklistTask() {
        List<Blocklist> res = new ArrayList<>();
        Mockito.when(blocklistMapper.selectByExample(Mockito.any())).thenReturn(res);
        boolean result = blocklistService.isBlocklistTask("project", "flow", "task");
        Assert.assertFalse(result);
    }
}
