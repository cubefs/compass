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

    List<FlinkTaskDiagnosis> selectByExampleWithBLOBs(FlinkTaskDiagnosisExample example);

    List<FlinkTaskDiagnosis> selectByExample(FlinkTaskDiagnosisExample example);

    FlinkTaskDiagnosis selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FlinkTaskDiagnosis record, @Param("example") FlinkTaskDiagnosisExample example);

    int updateByExampleWithBLOBs(@Param("record") FlinkTaskDiagnosis record, @Param("example") FlinkTaskDiagnosisExample example);

    int updateByExample(@Param("record") FlinkTaskDiagnosis record, @Param("example") FlinkTaskDiagnosisExample example);

    int updateByPrimaryKeySelective(FlinkTaskDiagnosis record);

    int updateByPrimaryKeyWithBLOBs(FlinkTaskDiagnosis record);

    int updateByPrimaryKey(FlinkTaskDiagnosis record);
}