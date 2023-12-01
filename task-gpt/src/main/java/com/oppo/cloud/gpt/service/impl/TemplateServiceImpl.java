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

package com.oppo.cloud.gpt.service.impl;

import com.github.pagehelper.PageHelper;
import com.oppo.cloud.gpt.config.DrainConfig;
import com.oppo.cloud.gpt.drain.*;
import com.oppo.cloud.gpt.service.TemplateService;
import com.oppo.cloud.gpt.util.UUIDIdGenerator;
import com.oppo.cloud.mapper.TemplateMapper;
import com.oppo.cloud.model.Template;
import com.oppo.cloud.model.TemplateExample;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private DrainConfig config;

    @Getter
    private Drain drain;

    private Mask masker;

    private Map<String, String> adviceMap;

    private IdGenerator idGen;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * It will aggregate log template
     * but not create or update template into the drain tree
     *
     * @param message
     * @return
     */
    @Override
    public LogCluster aggregate(String message) {
        String masked = this.masker.mask(message);
        this.lock.readLock().lock();
        try {
            return this.drain.aggregate(masked);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Match log message
     *
     * @param message
     * @param fullSearchStrategy: never, always, fallback
     * @return
     */
    @Override
    public LogCluster match(String message, String fullSearchStrategy) {
        String masked = this.masker.mask(message);
        // lock required
        this.lock.readLock().lock();
        try {
            return this.drain.match(masked, fullSearchStrategy);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * get advice
     *
     * @param cid
     * @return
     */
    @Override
    public String getAdvice(String cid) {
        return this.adviceMap.get(cid);
    }

    /**
     * initialize the drain object.
     */
    @PostConstruct
    public void init() {
        this.idGen = new UUIDIdGenerator();
        this.adviceMap = new ConcurrentHashMap<>();
        this.masker = new Mask(
                config.getMaskRules()
                        .stream()
                        .map(e -> new MaskRule(e.getRegex(), e.getMaskWith()))
                        .collect(Collectors.toList()),
                config.getMaskPrefix(),
                config.getMaskSuffix()
        );
        this.loadTemplates();
    }

    @Override
    public void loadTemplates() {
        Drain drain = createDrain();
        Map<String, String> adviceMap = new ConcurrentHashMap<>();
        List<Template> templates = this.queryTemplates();
        // lru cache: the latest one is put lastly.
        for (int index = templates.size() - 1; index >= 0; index--) {
            Template template = templates.get(index);
            LogCluster logCluster = drain.aggregate(template.getCluster()); // aggregate log again

            // There is the same template existing, delete it.
            if (logCluster.getState().equals(LogClusterState.NONE)) {
                this.delete(logCluster.getId());
                continue;
            }

            // There are two templates similar, merge templates, delete new one.
            if (logCluster.getState().equals(LogClusterState.CLUSTER_CHANGED)) {
                this.update(logCluster.getId(), logCluster.getTemplate(), null);
                this.delete(template.getCid());
            }

            // if log cluster state is created, id is empty.
            if (StringUtils.isBlank(logCluster.getId())) {
                logCluster.setId(template.getCid());
            }

            if (StringUtils.isBlank(template.getAdvice())) {
                log.warn("template: {} does not have advice.", template.getCid());
                continue;
            }
            drain.addCluster(logCluster);
            adviceMap.put(template.getCid(), template.getAdvice());
        }

        this.adviceMap.clear();
        this.adviceMap.putAll(adviceMap);

        this.lock.writeLock().lock();
        this.drain = drain;
        this.lock.writeLock().unlock();
    }

    public Drain createDrain() {
        return new Drain(
                config.getMaxDepth(),
                config.getSimilarityThreshold(),
                config.getMaxChildren(),
                config.getMaxClusters(),
                config.getMaxTokens(),
                config.getDelimiters(),
                config.getMaskPrefix() + "*" + config.getMaskSuffix(),
                true,
                idGen
        );
    }

    @Override
    public void save(Template template) {
        templateMapper.insert(template);
    }

    @Override
    public Template find(String cid) {
        TemplateExample example = new TemplateExample();
        example.or().andCidEqualTo(cid);
        List<Template> templates = templateMapper.selectByExampleWithBLOBs(example);
        return templates.isEmpty() ? null : templates.get(0);
    }

    @Override
    public void update(String cid, String cluster, String advice) {
        TemplateExample example = new TemplateExample();
        example.or().andCidEqualTo(cid);

        Template template = new Template();
        template.setCluster(cluster);
        template.setAdvice(advice);
        template.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        templateMapper.updateByExampleSelective(template, example);
    }

    @Override
    public void delete(String cid) {
        TemplateExample example = new TemplateExample();
        example.or().andCidEqualTo(cid);
        templateMapper.deleteByExample(example);
    }

    /**
     * query latest templates from table
     *
     * @return
     */
    @Override
    public List<Template> queryTemplates() {
        int limit = config.getMaxClusters() > 0 ? config.getMaxClusters() : 10000;
        PageHelper.startPage(1, limit, "update_time desc");
        return templateMapper.selectByExampleWithBLOBs(new TemplateExample());
    }

    /**
     * Merge two similar templates into one.
     *
     * @param seq1
     * @param seq2
     * @return
     */
    @Override
    public String[] mergeTemplates(String[] seq1, String[] seq2) {
        try {
            this.lock.readLock().lock();
            return this.drain.mergeTemplates(seq1, seq2);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Add template to drain tree
     *
     * @param logCluster
     * @param advice
     */
    @Override
    public void addTemplate(LogCluster logCluster, String advice) {
        this.lock.writeLock().lock();
        this.drain.addCluster(logCluster);
        this.adviceMap.put(logCluster.getId(), advice);
        this.lock.writeLock().unlock();
    }
}
