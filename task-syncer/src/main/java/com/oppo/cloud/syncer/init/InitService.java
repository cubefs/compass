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
 * Initialize service
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
     * Table data mapping rules
     */
    private Map<String, Mapping> tableMapping;


    @Override
    public void run(String... args) {
        List<TaskSyncerInit> taskSyncerInits = taskSyncerInitMapper.selectByExample(new TaskSyncerInitExample());
        if (taskSyncerInits != null && taskSyncerInits.size() > 0) {
            log.info("task-syncer has initialized ...");
            return;
        }

        // There may be concurrency issues and abnormal exit.
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
            //  save user table data
            initTable(mapping, (Map<String, String> data) -> {
                UserInfo user = (UserInfo) DataUtil.parseInstance(data, UserBuilder.class);
                try {
                    userMapper.save(user);
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
            // save project table data
            initTable(mapping, (Map<String, String> data) -> {
                Project project = (Project) DataUtil.parseInstance(data, ProjectBuilder.class);
                try {
                    projectMapper.save(project);
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
            // Save flow data table
            initTable(mapping, (Map<String, String> data) -> {
                Flow flow = (Flow) DataUtil.parseInstance(data, FlowBuilder.class);
                try {
                    flowMapper.save(flow);
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
                    taskMapper.save(task);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        }

    }

    /**
     * Initialize user table
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

            // value mapping
            DataUtil.mapColumnValue(datas, mapping.getColumnValueMapping());

            // Add constant column
            DataUtil.constantColumnValue(datas, mapping.getConstantColumn());

            for (Map<String, String> data : datas) {
                if (columnDep != null) {
                    for (String depQuery : columnDep.getQueries()) {
                        depQuery = StringUtil.replaceParams(depQuery, data);
                        Map<String, Object> result = null;
                        try {
                            result = jdbcTemplate.queryForMap(depQuery);
                        } catch (Exception e) {
                            log.error("failed to execute dep query: {},{}", depQuery, e.getMessage());
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
                // Save data
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
                    if (v instanceof LocalDateTime) {
                        v = DataUtil.formatDateObject(v);
                    }
                    data.put(key, v.toString());
                }
            }

            datas.add(data);
        }
        return datas;
    }

    /**
     * Build query sql
     */
    public String buildQuery(String table, Integer page, Integer pageSize) {
        return String.format("SELECT * FROM %s LIMIT %d,%d", table, (page - 1) * pageSize, pageSize);
    }

    /**
     * Get table field mapping rules
     */
    public Mapping getTableMapping(String table) {
        if (this.tableMapping == null) {
            initTableMapping();
        }
        return this.tableMapping.get(table);
    }

    /**
     * Load data table mapping rules
     */
    public Map<String, Mapping> loadTableMapping() {
        if (this.tableMapping == null) {
            initTableMapping();
        }
        return this.tableMapping;
    }

    /**
     * Initialization table mapping
     */
    public synchronized void initTableMapping() {
        this.tableMapping = new HashMap<>();
        for (Mapping mapping : this.dataSourceConfig.getMappings()) {
            this.tableMapping.put(mapping.getTargetTable(), mapping);
        }
    }

    /**
     * Data storage interface
     */
    interface DataStore {

        void call(Map<String, String> data);
    }
}
