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

import com.oppo.cloud.model.User;
import com.oppo.cloud.model.UserExample;
import com.oppo.cloud.syncer.dao.UserExtendMapper;
import com.oppo.cloud.syncer.domain.ColumnDep;
import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;
import com.oppo.cloud.syncer.service.ActionService;
import com.oppo.cloud.syncer.util.DataUtil;
import com.oppo.cloud.syncer.util.StringUtil;
import com.oppo.cloud.syncer.util.databuild.DataBuilder;
import com.oppo.cloud.syncer.util.databuild.DataFactory;
import com.oppo.cloud.syncer.util.databuild.GenericFactory;
import com.oppo.cloud.syncer.util.databuild.UserBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User table synchronization operation service
 */
@Slf4j
@Service
public class UserService extends CommonService implements ActionService {

    @Autowired
    private UserExtendMapper userMapper;

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
     * delete users
     */
    @Override
    public void delete(RawTable rawTable, Mapping mapping) {
        // do nothing
    }
    /**
     * Update user
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
        User instance = (User) DataUtil.parseInstance(data, UserBuilder.class);
        if (action.equals("INSERT")) {
            userMapper.saveSelective(instance);
        } else if (action.equals("UPDATE")) {
            userMapper.updateByExampleSelective(instance, buildUserExample(data));
        }
    }
    /**
     * Build update query conditions
     */
    public UserExample buildUserExample(Map<String, String> data) {
        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(data.get("user_id"));
        return example;
    }
}
