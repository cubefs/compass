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

package com.oppo.cloud.syncer.util.databuild;

import com.oppo.cloud.model.User;
import com.oppo.cloud.syncer.util.DataUtil;

import java.util.Map;

/**
 * User build
 */
public class UserBuilder implements DataBuilder<User> {

    @Override
    public User run(Map<String, String> data) {
        User user = new User();
        user.setUserId(DataUtil.parseInteger(data.get("user_id")));
        user.setUsername(data.get("username"));
        user.setPassword(data.get("password"));
        user.setIsAdmin(DataUtil.parseInteger(data.get("is_admin")));
        user.setEmail(data.get("email"));
        user.setPhone(data.get("phone"));
        user.setUpdateTime(DataUtil.parseDate(data.get("update_time")));
        user.setCreateTime(DataUtil.parseDate(data.get("create_time")));
        user.setSchedulerType(data.get("scheduler_type"));

        return user;
    }
}
