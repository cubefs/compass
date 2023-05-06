package com.oppo.cloud.mapper;

import com.oppo.cloud.model.RealtimeTaskDiagnosisRuleAdvice;
import com.oppo.cloud.model.RealtimeTaskDiagnosisRuleAdviceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskDiagnosisRuleAdviceMapper {
    long countByExample(RealtimeTaskDiagnosisRuleAdviceExample example);

    int deleteByExample(RealtimeTaskDiagnosisRuleAdviceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RealtimeTaskDiagnosisRuleAdvice record);

    int insertSelective(RealtimeTaskDiagnosisRuleAdvice record);

    List<RealtimeTaskDiagnosisRuleAdvice> selectByExample(RealtimeTaskDiagnosisRuleAdviceExample example);

    RealtimeTaskDiagnosisRuleAdvice selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RealtimeTaskDiagnosisRuleAdvice record, @Param("example") RealtimeTaskDiagnosisRuleAdviceExample example);

    int updateByExample(@Param("record") RealtimeTaskDiagnosisRuleAdvice record, @Param("example") RealtimeTaskDiagnosisRuleAdviceExample example);

    int updateByPrimaryKeySelective(RealtimeTaskDiagnosisRuleAdvice record);

    int updateByPrimaryKey(RealtimeTaskDiagnosisRuleAdvice record);
}