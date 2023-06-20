package com.oppo.cloud.mapper;

import com.oppo.cloud.model.FlinkTask;
import com.oppo.cloud.model.FlinkTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskMapper {
    long countByExample(FlinkTaskExample example);

    int deleteByExample(FlinkTaskExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(FlinkTask record);

    int insertSelective(FlinkTask record);

    List<FlinkTask> selectByExample(FlinkTaskExample example);

    FlinkTask selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FlinkTask record, @Param("example") FlinkTaskExample example);

    int updateByExample(@Param("record") FlinkTask record, @Param("example") FlinkTaskExample example);

    int updateByPrimaryKeySelective(FlinkTask record);

    int updateByPrimaryKey(FlinkTask record);
}