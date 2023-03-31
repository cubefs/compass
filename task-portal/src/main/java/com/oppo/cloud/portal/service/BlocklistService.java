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

package com.oppo.cloud.portal.service;

import com.oppo.cloud.model.Blocklist;
import com.oppo.cloud.model.Task;
import com.oppo.cloud.portal.domain.blocklist.BlocklistAddReq;
import com.oppo.cloud.portal.domain.blocklist.BlocklistReq;

import java.util.List;

/**
 * 白名单管理Service
 */
public interface BlocklistService {

    /**
     * 分页查询白名单列表
     */
    List<Blocklist> search(BlocklistReq blocklistReq) throws Exception;

    /**
     *批量删除任务白名单
     */
    void deleteByIds(List<Integer> blocklistIds) throws Exception;

    /**
     * 任务查询
     */
    List<Task> searchTasks(BlocklistAddReq blocklistAddReq) throws Exception;

    /**
     * 添加任务白名单
     */
    void addBlocklist(BlocklistAddReq blocklistAddReq) throws Exception;

}
