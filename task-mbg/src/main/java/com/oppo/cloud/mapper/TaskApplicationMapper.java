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

import com.oppo.cloud.model.TaskApplication;
import com.oppo.cloud.model.TaskApplicationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaskApplicationMapper {

    long countByExample(TaskApplicationExample example);

    int deleteByExample(TaskApplicationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TaskApplication record);

    int insertSelective(TaskApplication record);

    List<TaskApplication> selectByExampleWithBLOBs(TaskApplicationExample example);

    List<TaskApplication> selectByExample(TaskApplicationExample example);

    TaskApplication selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TaskApplication record,
                                 @Param("example") TaskApplicationExample example);

    int updateByExampleWithBLOBs(@Param("record") TaskApplication record,
                                 @Param("example") TaskApplicationExample example);

    int updateByExample(@Param("record") TaskApplication record, @Param("example") TaskApplicationExample example);

    int updateByPrimaryKeySelective(TaskApplication record);

    int updateByPrimaryKeyWithBLOBs(TaskApplication record);

    int updateByPrimaryKey(TaskApplication record);
}
