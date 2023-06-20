package com.oppo.cloud.mapper;

import com.oppo.cloud.model.FlinkTaskDiagnosisRuleAdvice;
import com.oppo.cloud.model.FlinkTaskDiagnosisRuleAdviceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskDiagnosisRuleAdviceMapper {
    long countByExample(FlinkTaskDiagnosisRuleAdviceExample example);

    int deleteByExample(FlinkTaskDiagnosisRuleAdviceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(FlinkTaskDiagnosisRuleAdvice record);

    int insertSelective(FlinkTaskDiagnosisRuleAdvice record);

    List<FlinkTaskDiagnosisRuleAdvice> selectByExample(FlinkTaskDiagnosisRuleAdviceExample example);

    FlinkTaskDiagnosisRuleAdvice selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FlinkTaskDiagnosisRuleAdvice record, @Param("example") FlinkTaskDiagnosisRuleAdviceExample example);

    int updateByExample(@Param("record") FlinkTaskDiagnosisRuleAdvice record, @Param("example") FlinkTaskDiagnosisRuleAdviceExample example);

    int updateByPrimaryKeySelective(FlinkTaskDiagnosisRuleAdvice record);

    int updateByPrimaryKey(FlinkTaskDiagnosisRuleAdvice record);
}