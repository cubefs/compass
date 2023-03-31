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

package com.oppo.cloud.portal.controller;

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.portal.domain.task.UserInfo;
import com.oppo.cloud.portal.domain.user.LoginRequest;
import com.oppo.cloud.portal.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 用户管理
 */

@Controller
@Api(value = "UserController", description = "用户管理")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     */
    @ApiOperation("用户登录接口")
    @PostMapping(value = "/login")
    @ResponseBody
    public CommonStatus<UserInfo> login(HttpServletResponse httpServletResponse, @RequestBody LoginRequest loginRequest) {
        try {
            return CommonStatus.success(userService.userLogin(httpServletResponse, loginRequest.getUsername(),
                    loginRequest.getPassword()));
        } catch (Exception e) {
            return CommonStatus.failed(e.getMessage());
        }
    }


}
