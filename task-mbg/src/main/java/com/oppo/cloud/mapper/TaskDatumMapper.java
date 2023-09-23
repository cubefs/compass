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

import com.oppo.cloud.model.TaskDatum;
import com.oppo.cloud.model.TaskDatumExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaskDatumMapper {

    long countByExample(TaskDatumExample example);

    int deleteByExample(TaskDatumExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TaskDatum record);

    int insertSelective(TaskDatum record);

    List<TaskDatum> selectByExampleWithBLOBs(TaskDatumExample example);

    List<TaskDatum> selectByExample(TaskDatumExample example);

    TaskDatum selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TaskDatum record, @Param("example") TaskDatumExample example);

    int updateByExampleWithBLOBs(@Param("record") TaskDatum record, @Param("example") TaskDatumExample example);

    int updateByExample(@Param("record") TaskDatum record, @Param("example") TaskDatumExample example);

    int updateByPrimaryKeySelective(TaskDatum record);

    int updateByPrimaryKeyWithBLOBs(TaskDatum record);

    int updateByPrimaryKey(TaskDatum record);
}
