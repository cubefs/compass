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

import com.oppo.cloud.common.constant.SchedulerType;
import com.oppo.cloud.mapper.UserInfoMapper;
import com.oppo.cloud.model.UserInfo;
import com.oppo.cloud.model.UserInfoExample;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.domain.task.UserResponse;
import com.oppo.cloud.portal.service.ProjectService;
import com.oppo.cloud.portal.service.UserService;
import com.oppo.cloud.portal.util.CryptoUtil;
import com.oppo.cloud.portal.util.EncryptionUtils;
import com.oppo.cloud.portal.util.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private UserInfoMapper userMapper;

    /**
     * Get user information by username
     */
    @Override
    public UserInfo getByUsername(String username) {
        UserInfoExample example = new UserInfoExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UserInfo> users = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        return users.get(0);
    }

    /**
     * Login
     */
    @Override
    public UserResponse userLogin(HttpServletResponse httpServletResponse, String username, String password)
            throws Exception {

        UserInfo user = getByUsername(username);
        if (user == null) {
            throw new Exception("用户名不存在");
        }
        String schedulerType = user.getSchedulerType();
        boolean loginFlag;
        String hashPassword = user.getPassword();

        if (SchedulerType.DolphinScheduler.toString().equalsIgnoreCase(schedulerType)) {
            String md5HashPassword = EncryptionUtils.getMd5(password);
            loginFlag = md5HashPassword.equals(hashPassword);
        } else if (SchedulerType.Airflow.toString().equalsIgnoreCase(schedulerType)) {
            loginFlag = CryptoUtil.checkPassword(password, hashPassword);
        } else {
            // todo customization requires self-implementation
            loginFlag = true;
        }
        if (!loginFlag) {
            throw new Exception("用户名或密码错误");
        }

        UserResponse userInfo = new UserResponse();
        userInfo.setAdmin(user.getIsAdmin() == 0);
        userInfo.setUsername(user.getUsername());
        userInfo.setSchedulerType(schedulerType);
        userInfo.setToken(jwtUtil.createToken(user));
        ThreadLocalUserInfo.set(userInfo);
        return userInfo;
    }


}
