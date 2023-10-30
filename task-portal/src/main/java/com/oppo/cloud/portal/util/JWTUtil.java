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

package com.oppo.cloud.portal.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.model.UserInfo;
import com.oppo.cloud.portal.domain.task.UserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {
    @Value("${custom.jwt.expireDay}")
    private int expireDay = 7;
    @Value("${custom.jwt.secret}")
    private String secret;

    public String createToken(UserInfo user) throws Exception {
        boolean isAdmin = user.getIsAdmin() == 0;
        return JWT.create()
                .withIssuer("compass")
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("isAdmin", isAdmin)
                .withClaim("schedulerType", user.getSchedulerType())
                .withExpiresAt(DateUtil.getOffsetDate(new Date(), expireDay))
                .sign(Algorithm.HMAC256(secret));
    }

    public UserInfoResponse verifyToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("compass")
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);
        Integer userId = decodedJWT.getClaim("userId").asInt();
        String username = decodedJWT.getClaim("username").asString();
        Boolean isAdmin = decodedJWT.getClaim("isAdmin").asBoolean();
        String schedulerType = decodedJWT.getClaim("schedulerType").asString();
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setUserId(userId);
        userInfo.setUsername(username);
        userInfo.setAdmin(isAdmin);
        userInfo.setSchedulerType(schedulerType);
        return userInfo;
    }

}
