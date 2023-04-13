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

package com.oppo.cloud.syncer.init;

import com.oppo.cloud.mapper.*;
import com.oppo.cloud.model.*;
import com.oppo.cloud.syncer.config.DataSourceConfig;
import com.oppo.cloud.syncer.dao.*;
import com.oppo.cloud.syncer.domain.ColumnDep;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.service.impl.TaskInstanceService;
import com.oppo.cloud.syncer.util.DataUtil;
import com.oppo.cloud.syncer.util.StringUtil;
import com.oppo.cloud.syncer.util.databuild.FlowBuilder;
import com.oppo.cloud.syncer.util.databuild.ProjectBuilder;
import com.oppo.cloud.syncer.util.databuild.TaskBuilder;
import com.oppo.cloud.syncer.util.databuild.UserBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化服务
 */
@Slf4j
@Component
public class InitService implements CommandLineRunner {

    @Autowired
    private UserExtendMapper userMapper;

    @Autowired
    private ProjectExtendMapper projectMapper;

    @Autowired
    private FlowExtendMapper flowMapper;

    @Autowired
    private TaskExtendMapper taskMapper;

    @Autowired
    private TaskInstanceExtendMapper taskInstanceMapper;

    @Autowired
    private TaskSyncerInitMapper taskSyncerInitMapper;

    @Autowired
    @Qualifier("diagnoseJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("sourceJdbcTemplate")
    private JdbcTemplate sourceJdbcTemplate;

    @Resource
    private DataSourceConfig dataSourceConfig;

    /**
     * 表数据映射规则
     */
    private Map<String, Mapping> tableMapping;


    @Override
    public void run(String... args) {
        List<TaskSyncerInit> taskSyncerInits = taskSyncerInitMapper.selectByExample(new TaskSyncerInitExample());
        if (taskSyncerInits != null && taskSyncerInits.size() > 0) {
            log.info("task-syncer has initialized ...");
            return;
        }

        // 可能有并发问题, 异常退出
        try {
            TaskSyncerInit taskSyncerInit = new TaskSyncerInit();
            taskSyncerInit.setIsInit(1);
            taskSyncerInitMapper.insert(taskSyncerInit);
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        Mapping mapping = null;
        // target table = user
        mapping = this.getTableMapping("user");
        if (mapping == null) {
            log.error("can not find `user` table mapping");
        } else {
            // 保存用户数据表
            initTable(mapping, (Map<String, String> data) -> {
                User user = (User) DataUtil.parseInstance(data, UserBuilder.class);
                try {
                    userMapper.saveSelective(user);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        }
        // target table = project
        mapping = this.getTableMapping("project");
        if (mapping == null) {
            log.error("can not find `project` table mapping");
        } else {
            // 保存项目数据表
            initTable(mapping, (Map<String, String> data) -> {
                Project project = (Project) DataUtil.parseInstance(data, ProjectBuilder.class);
                try {
                    projectMapper.saveSelective(project);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        }

        // target table = flow
        mapping = this.getTableMapping("flow");
        if (mapping == null) {
            log.error("can not find `flow` table mapping");
        } else {
            // 保存flow数据表
            initTable(mapping, (Map<String, String> data) -> {
                Flow flow = (Flow) DataUtil.parseInstance(data, FlowBuilder.class);
                try {
                    flowMapper.saveSelective(flow);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        }

        // target table = task
        mapping = this.getTableMapping("task");
        if (mapping == null) {
            log.error("can not find `task` table mapping");
        } else {
            // save task table data
            initTable(mapping, (Map<String, String> data) -> {
                Task task = (Task) DataUtil.parseInstance(data, TaskBuilder.class);
                try {
                    taskMapper.saveSelective(task);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        }

    }

    /**
     * 初始化用户表
     */
    public void initTable(Mapping mapping, DataStore dataStore) {
        int page = 1;
        int pageSize = 100;

        ColumnDep columnDep = mapping.getColumnDep();

        while (true) {
            String query = buildQuery(mapping.getTable(), page, pageSize);
            List<Map<String, Object>> queryDatas = sourceJdbcTemplate.queryForList(query);
            if (queryDatas.size() == 0) {
                break;
            }

            List<Map<String, String>> datas = this.convertMap(queryDatas);

            datas = DataUtil.mapData(datas, mapping.getColumnMapping());

            // 值映射
            DataUtil.mapColumnValue(datas, mapping.getColumnValueMapping());

            // 增加常数列
            DataUtil.constantColumnValue(datas, mapping.getConstantColumn());

            for (Map<String, String> data : datas) {
                if (columnDep != null) {
                    for (String depQuery : columnDep.getQueries()) {
                        depQuery = StringUtil.replaceParams(depQuery, data);
                        Map<String, Object> result = null;
                        try {
                            result = jdbcTemplate.queryForMap(depQuery);
                        } catch (Exception e) {
                            log.error("failed to execute dep query: " + e.getMessage());
                        }
                        if (result == null) {
                            continue;
                        }
                        for (String key : result.keySet()) {
                            Object v = result.get(key);
                            if (v == null) {
                                continue;
                            }
                            if (v instanceof LocalDateTime) {
                                v = DataUtil.formatDateObject(v);
                            }
                            data.put(key, v.toString());
                        }
                    }
                }
                // 保存数据
                dataStore.call(data);
            }

            page += 1;
        }
    }

    public List<Map<String, String>> convertMap(List<Map<String, Object>> queryDatas) {
        List<Map<String, String>> datas = new ArrayList<>();

        for (Map<String, Object> queryData : queryDatas) {
            Map<String, String> data = new HashMap<>();
            for (String key : queryData.keySet()) {
                Object v = queryData.get(key);
                if (v == null) {
                    data.put(key, null);
                } else {
                    data.put(key, v.toString());
                }
            }

            datas.add(data);
        }
        return datas;
    }

    /**
     * 构建查询sql
     */
    public String buildQuery(String table, Integer page, Integer pageSize) {
        return String.format("SELECT * FROM %s LIMIT %d,%d", table, (page - 1) * pageSize, pageSize);
    }

    /**
     * 获取表字段映射规则
     */
    public Mapping getTableMapping(String table) {
        if (this.tableMapping == null) {
            initTableMapping();
        }
        return this.tableMapping.get(table);
    }

    /**
     * 加载数据表映射规则
     */
    public Map<String, Mapping> loadTableMapping() {
        if (this.tableMapping == null) {
            initTableMapping();
        }
        return this.tableMapping;
    }

    /**
     * 初始化表映射
     */
    public synchronized void initTableMapping() {
        this.tableMapping = new HashMap<>();
        for (Mapping mapping : this.dataSourceConfig.getMappings()) {
            this.tableMapping.put(mapping.getTargetTable(), mapping);
        }
    }

    /**
     * 数据存储接口
     */
    interface DataStore {

        void call(Map<String, String> data);
    }
}
