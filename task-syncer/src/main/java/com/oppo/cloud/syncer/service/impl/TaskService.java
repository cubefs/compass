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

package com.oppo.cloud.syncer.service.impl;

import com.oppo.cloud.model.Flow;
import com.oppo.cloud.model.Task;
import com.oppo.cloud.model.TaskExample;
import com.oppo.cloud.syncer.dao.TaskExtendMapper;
import com.oppo.cloud.syncer.domain.ColumnDep;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;
import com.oppo.cloud.syncer.service.ActionService;
import com.oppo.cloud.syncer.util.DataUtil;
import com.oppo.cloud.syncer.util.StringUtil;
import com.oppo.cloud.syncer.util.databuild.FlowBuilder;
import com.oppo.cloud.syncer.util.databuild.TaskBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Task definition or job definition data table synchronization service
 */
@Service
public class TaskService extends CommonService implements ActionService {

    @Autowired
    private TaskExtendMapper taskMapper;

    @Autowired
    @Qualifier("diagnoseJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * insert operation
     */
    @Override
    public void insert(RawTable rawTable, Mapping mapping) {
        dataMapping(jdbcTemplate, rawTable, mapping, "INSERT");

    }
    /**
     * Delete operation
     */
    @Override
    public void delete(RawTable rawTable, Mapping mapping) {

    }
    /**
     * update operation
     */
    @Override
    public void update(RawTable rawTable, Mapping mapping) {
        dataMapping(jdbcTemplate, rawTable, mapping, "UPDATE");
    }
    /**
     * Data saving
     */
    @Override
    public void dataSave(Map<String, String> data, Mapping mapping, String action) {
        Task instance = (Task) DataUtil.parseInstance(data, TaskBuilder.class);
        if (instance.getId() != null) {
            if ("INSERT".equals(action)) {
                taskMapper.saveSelective(instance);
            } else if ("UPDATE".equals(action)) {
                taskMapper.updateByPrimaryKeySelective(instance);
            }
        } else {
            if ("INSERT".equals(action)) {
                if (getTask(instance) == null) {
                    taskMapper.saveSelective(instance);
                }
            } else if ("UPDATE".equals(action)) {
                taskMapper.updateByTask(instance);
            }
        }
    }

    public Task getTask(Task task) {
        List<Task> tasks = taskMapper.selectByTask(task);
        if (tasks.size() > 0) {
            return tasks.get(0);
        }
        return null;
    }
}
