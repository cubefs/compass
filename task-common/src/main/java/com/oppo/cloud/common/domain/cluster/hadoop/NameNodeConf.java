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

package com.oppo.cloud.common.domain.cluster.hadoop;

import lombok.Data;

/**
 * name node 集群配置信息
 */
@Data
public class NameNodeConf {

    /**
     * 命名空间
     */
    private String nameservices;

    /**
     * namenode地址
     */
    private String[] namenodesAddr;

    /**
     * namenode名称
     */
    private String[] namenodes;

    /**
     * 用户
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * 端口
     */
    private String port;


    /**
     * 匹配路径
     */
    private String[] matchPathKeys;

    private boolean enableKerberos;

    private String krb5Conf;

    private String principalPattern;

    private String loginUser;

    private String keytabPath;
}
