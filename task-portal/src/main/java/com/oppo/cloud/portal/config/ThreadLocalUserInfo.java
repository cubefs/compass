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

package com.oppo.cloud.portal.config;

import com.oppo.cloud.portal.domain.task.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 *  同一个请求中保存用户信息到当前线程中
 */
public class ThreadLocalUserInfo {

    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

    /**
     * 存储用户信息
     */
    public static void set(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    /**
     * 获取当前用户信息
     */
    public static UserInfo getCurrentUser() {
        return USER_THREAD_LOCAL.get();
    }
}
