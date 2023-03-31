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

package com.oppo.cloud.syncer.dao;

import com.oppo.cloud.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
@ComponentScan(basePackages = "com.oppo.cloud")
public class TestUserExtendMapper {

    @Autowired
    private UserExtendMapper userExtendMapper;

    @Test
    public void testSave() {
        User user = new User();
        user.setUserId(200);
        user.setUsername("testUsername");
        user.setEmail("test@email.com");
        user.setIsAdmin(0);
        user.setPassword("myPassword");

        userExtendMapper.save(user);
    }
}
