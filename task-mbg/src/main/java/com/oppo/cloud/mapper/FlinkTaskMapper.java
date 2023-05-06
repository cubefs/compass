package com.oppo.cloud.mapper;

import com.oppo.cloud.model.FlinkTask;
import com.oppo.cloud.model.RealtimeTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskMapper {
    long countByExample(RealtimeTaskExample example);

    int deleteByExample(RealtimeTaskExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(FlinkTask record);

    int insertSelective(FlinkTask record);

    List<FlinkTask> selectByExample(RealtimeTaskExample example);

    FlinkTask selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FlinkTask record, @Param("example") RealtimeTaskExample example);

    int updateByExample(@Param("record") FlinkTask record, @Param("example") RealtimeTaskExample example);

    int updateByPrimaryKeySelective(FlinkTask record);

    int updateByPrimaryKey(FlinkTask record);
}