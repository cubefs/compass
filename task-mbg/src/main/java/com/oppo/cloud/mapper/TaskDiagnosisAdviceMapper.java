package com.oppo.cloud.mapper;

import com.oppo.cloud.model.TaskDiagnosisAdvice;
import com.oppo.cloud.model.TaskDiagnosisAdviceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TaskDiagnosisAdviceMapper {
    long countByExample(TaskDiagnosisAdviceExample example);

    int deleteByExample(TaskDiagnosisAdviceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TaskDiagnosisAdvice record);

    int insertSelective(TaskDiagnosisAdvice record);

    List<TaskDiagnosisAdvice> selectByExampleWithBLOBs(TaskDiagnosisAdviceExample example);

    List<TaskDiagnosisAdvice> selectByExample(TaskDiagnosisAdviceExample example);

    TaskDiagnosisAdvice selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TaskDiagnosisAdvice record, @Param("example") TaskDiagnosisAdviceExample example);

    int updateByExampleWithBLOBs(@Param("record") TaskDiagnosisAdvice record, @Param("example") TaskDiagnosisAdviceExample example);

    int updateByExample(@Param("record") TaskDiagnosisAdvice record, @Param("example") TaskDiagnosisAdviceExample example);

    int updateByPrimaryKeySelective(TaskDiagnosisAdvice record);

    int updateByPrimaryKeyWithBLOBs(TaskDiagnosisAdvice record);

    int updateByPrimaryKey(TaskDiagnosisAdvice record);
}