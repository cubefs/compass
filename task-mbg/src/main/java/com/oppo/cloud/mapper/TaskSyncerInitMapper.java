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

package com.oppo.cloud.mapper;

import com.oppo.cloud.model.TaskSyncerInit;
import com.oppo.cloud.model.TaskSyncerInitExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaskSyncerInitMapper {

    long countByExample(TaskSyncerInitExample example);

    int deleteByExample(TaskSyncerInitExample example);

    int insert(TaskSyncerInit record);

    int insertSelective(TaskSyncerInit record);

    List<TaskSyncerInit> selectByExample(TaskSyncerInitExample example);

    int updateByExampleSelective(@Param("record") TaskSyncerInit record,
                                 @Param("example") TaskSyncerInitExample example);

    int updateByExample(@Param("record") TaskSyncerInit record, @Param("example") TaskSyncerInitExample example);
}
