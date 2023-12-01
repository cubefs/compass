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

package com.oppo.cloud.gpt.service;

import com.oppo.cloud.model.Template;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TemplateServiceImplTest {

    @Autowired
    private TemplateService templateService;

    @Test
    public void testLoadTemplates() {
        // check query syntax
        templateService.queryTemplates();
    }

    @Test
    public void updateTemplate() {
        String cid = "30b0f6173e5b426c8c0590758f802925";
        String cluster = "cluster test content";
        templateService.update(cid, cluster, null);
    }

    @Test
    public void updateTime() {
        // only update time
        String cid = "46af0cc3fd7a416d937493bd53643415";
        templateService.update(cid, null, null);
    }

    @Test
    public void deleteTemplate() {
        String cid = "30b0f6173e5b426c8c0590758f802925";
        templateService.delete(cid);
    }
}
