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

import com.oppo.cloud.model.FlinkTaskDiagnosis;
import com.oppo.cloud.model.FlinkTaskDiagnosisExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskDiagnosisMapper {
    long countByExample(FlinkTaskDiagnosisExample example);

    int deleteByExample(FlinkTaskDiagnosisExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(FlinkTaskDiagnosis record);

    int insertSelective(FlinkTaskDiagnosis record);

    List<FlinkTaskDiagnosis> selectByExample(FlinkTaskDiagnosisExample example);

    FlinkTaskDiagnosis selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FlinkTaskDiagnosis record, @Param("example") FlinkTaskDiagnosisExample example);

    int updateByExample(@Param("record") FlinkTaskDiagnosis record, @Param("example") FlinkTaskDiagnosisExample example);

    int updateByPrimaryKeySelective(FlinkTaskDiagnosis record);

    int updateByPrimaryKey(FlinkTaskDiagnosis record);
}