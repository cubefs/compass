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

package com.oppo.cloud.portal.interceptor;

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.domain.task.UserResponse;
import com.oppo.cloud.portal.util.JWTUtil;
import com.oppo.cloud.portal.util.MessageSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * User login interceptor
 */
@Component
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final String VERIFY_KEY = "/api";
    private static final String DOCS_KEY = "api-docs";
    private static final String TOKEN = "token";
    private static final String LANGUAGE = "language";

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) {

        if (!request.getRequestURI().contains(VERIFY_KEY) || request.getRequestURI().endsWith(DOCS_KEY)) {
            return true;
        }

        String token = request.getHeader(TOKEN);
        if (StringUtils.isEmpty(token)) {
            handleFalseResponse(response, MessageSourceUtil.get("TOKEN_VERIFY_FAILED"));
            return false;
        }

        UserResponse userInfo;
        try {
            userInfo = jwtUtil.verifyToken(token);
        } catch (Exception e) {
            handleFalseResponse(response, MessageSourceUtil.get("TOKEN_VERIFY_FAILED"));
            return false;
        }
        log.info("userInfo:{}", userInfo);
        ThreadLocalUserInfo.set(userInfo);

        String language = request.getHeader(LANGUAGE);
        log.info("language:{}", language);
        if (StringUtils.isNotBlank(token)) {
            LocaleContextHolder.setLocale(org.springframework.util.StringUtils.parseLocale(language));
            LocaleContextHolder.setDefaultLocale(org.springframework.util.StringUtils.parseLocale(language));
            log.info("getLocale:{}",  LocaleContextHolder.getLocale());
        }

        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {
        ThreadLocalUserInfo.clear();
    }

    /**
     * handle exception response
     */
    private void handleFalseResponse(HttpServletResponse response, String msg) {
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        CommonStatus<String> commonResult = CommonStatus.unauthorized(msg);
        try {
            response.getWriter().write(commonResult.toString());
        } catch (Exception e) {
            log.error("write error msg to response failed");
        }
    }

}
