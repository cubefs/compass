package com.oppo.cloud.gpt.service;

import com.oppo.cloud.gpt.drain.LogCluster;
import com.oppo.cloud.model.Template;

import java.util.List;

public interface TemplateService {

    void delete(String cid);

    void update(String cid, String cluster);

    void save(Template template);

    Template find(String cid);

    LogCluster aggregate(String message);

    LogCluster match(String message, String fullSearchStrategy);

    String getAdvice(String cid);

    void loadTemplates();

    List<Template> queryTemplates();

    String[] mergeTemplates(String[] seq1, String[] seq2);

    void addTemplate(LogCluster logCluster, String advice);
}
