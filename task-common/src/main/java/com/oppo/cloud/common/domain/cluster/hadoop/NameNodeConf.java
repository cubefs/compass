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
 * name node cluster configuration information
 */
@Data
public class NameNodeConf {

    /**
     * namespace
     */
    private String nameservices;

    /**
     * namenode address
     */
    private String[] namenodesAddr;

    /**
     * name of namenode
     */
    private String[] namenodes;

    /**
     * user
     */
    private String user;

    /**
     * password
     */
    private String password;

    /**
     * port
     */
    private String port;


    /**
     * matching path
     */
    private String[] matchPathKeys;

    private boolean enableKerberos;

    private String krb5Conf;

    private String principalPattern;

    private String loginUser;

    private String keytabPath;
}
