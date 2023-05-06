package com.oppo.cloud.mapper;

import com.oppo.cloud.model.RealtimeTaskDiagnosis;
import com.oppo.cloud.model.RealtimeTaskDiagnosisExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskDiagnosisMapper {
    long countByExample(RealtimeTaskDiagnosisExample example);

    int deleteByExample(RealtimeTaskDiagnosisExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RealtimeTaskDiagnosis record);

    int insertSelective(RealtimeTaskDiagnosis record);

    List<RealtimeTaskDiagnosis> selectByExampleWithBLOBs(RealtimeTaskDiagnosisExample example);

    List<RealtimeTaskDiagnosis> selectByExample(RealtimeTaskDiagnosisExample example);

    RealtimeTaskDiagnosis selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RealtimeTaskDiagnosis record, @Param("example") RealtimeTaskDiagnosisExample example);

    int updateByExampleWithBLOBs(@Param("record") RealtimeTaskDiagnosis record, @Param("example") RealtimeTaskDiagnosisExample example);

    int updateByExample(@Param("record") RealtimeTaskDiagnosis record, @Param("example") RealtimeTaskDiagnosisExample example);

    int updateByPrimaryKeySelective(RealtimeTaskDiagnosis record);

    int updateByPrimaryKeyWithBLOBs(RealtimeTaskDiagnosis record);

    int updateByPrimaryKey(RealtimeTaskDiagnosis record);
}