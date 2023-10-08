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

import com.oppo.cloud.model.Project;
import com.oppo.cloud.syncer.dao.ProjectExtendMapper;
import com.oppo.cloud.syncer.domain.ColumnDep;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;
import com.oppo.cloud.syncer.service.ActionService;
import com.oppo.cloud.syncer.util.DataUtil;
import com.oppo.cloud.syncer.util.StringUtil;
import com.oppo.cloud.syncer.util.databuild.ProjectBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Project table synchronization operation
 */
@Slf4j
@Service
public class ProjectService extends CommonService implements ActionService {

    @Autowired
    private ProjectExtendMapper projectMapper;

    @Autowired
    @Qualifier("diagnoseJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert data
     */
    @Override
    public void insert(RawTable rawTable, Mapping mapping) {
        dataMapping(jdbcTemplate, rawTable, mapping, "INSERT");
    }
    /**
     * delete data
     */
    @Override
    public void delete(RawTable rawTable, Mapping mapping) {
        // todo: delete
    }
    /**
     * update data
     */
    @Override
    public void update(RawTable rawTable, Mapping mapping) {
        dataMapping(jdbcTemplate, rawTable, mapping, "UPDATE");
        // TODO: update other relative table
    }
    /**
     * Data saving
     */
    @Override
    public void dataSave(Map<String, String> data, Mapping mapping, String action) {
        Project instance = (Project) DataUtil.parseInstance(data, ProjectBuilder.class);
        if ("INSERT".equals(action)) {
            projectMapper.saveSelective(instance);
        } else if ("UPDATE".equals(action)) {
            projectMapper.updateByPrimaryKeySelective(instance);
        }
    }
}
