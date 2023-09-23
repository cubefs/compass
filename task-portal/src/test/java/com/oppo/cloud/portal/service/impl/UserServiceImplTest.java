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

package com.oppo.cloud.portal.service.impl;

import com.oppo.cloud.mapper.UserMapper;
import com.oppo.cloud.model.User;
import com.oppo.cloud.portal.service.UserService;
import com.oppo.cloud.portal.util.EncryptionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UserServiceImplTest {

    @MockBean(name = "userMapper")
    UserMapper userMapper;

    @Autowired
    UserService userService;

    @Test
    void getUser() {
        List<User> users = new ArrayList<>();
        users.add(new User());
        Mockito.when(userMapper.selectByExample(Mockito.any())).thenReturn(users);
        User user = userService.getByUsername("username_test");
        Assertions.assertNotNull(user);
    }

    /**
     * dolphin 密码加密验证
     */
    @Test
    void md5CheckPassword() {
        String password = "dolphinscheduler123";
        String md5HashPassword = EncryptionUtils.getMd5(password);
        boolean check = md5HashPassword.equals("7ad2410b2f4c074479a8937a28a22b8f");
        System.out.println(check);
        Assertions.assertTrue(check);
    }
}
