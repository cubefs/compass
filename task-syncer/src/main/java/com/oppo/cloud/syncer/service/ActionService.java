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

package com.oppo.cloud.syncer.service;

import com.oppo.cloud.syncer.domain.Mapping;
import com.oppo.cloud.syncer.domain.RawTable;

/**
 * 数据表操作同步服务
 */
public interface ActionService {

    /**
     * 插入操作
     */
    void insert(RawTable rawTable, Mapping mapping);
    /**
     * 删除操作
     */
    void delete(RawTable rawTable, Mapping mapping);
    /**
     * 更新操作
     */
    void update(RawTable rawTable, Mapping mapping);
}
