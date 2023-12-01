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
