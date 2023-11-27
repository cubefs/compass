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
