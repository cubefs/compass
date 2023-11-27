package com.oppo.cloud.gpt.dao;

import com.oppo.cloud.gpt.util.UUIDIdGenerator;
import com.oppo.cloud.model.Template;
import com.oppo.cloud.model.TemplateExample;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.List;

@SpringBootTest
public class TemplateExtendMapperTest {

    @Autowired
    private TemplateExtendMapper templateMapper;

    @Test
    public void save() {
        Template template = new Template();
        template.setCid(new UUIDIdGenerator().next());
        template.setCluster("test");
        template.setRawLog("test");
        template.setCreateTime(new Timestamp(System.currentTimeMillis()));
        template.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        templateMapper.save(template);
    }

    @Test
    public void query() {
        TemplateExample example = new TemplateExample();
        List<Template> templates = templateMapper.selectByExampleWithBLOBs(example);
        System.out.println(templates.get(0));
    }
}
