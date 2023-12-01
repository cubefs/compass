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

package com.oppo.cloud.gpt.scheduled;

import com.oppo.cloud.gpt.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Loading template into drain with interval time.
 */
@Slf4j
@Service
public class TemplateLoader {

    @Autowired
    private TemplateService templateService;

    @Scheduled(cron = "${template.reload.cron}")
    public void reloadTemplates() {
        log.info("Start to reload templates >>>");
        templateService.loadTemplates();
        log.info("End to reload templates <<<");
    }
}
