package com.oppo.cloud.mapper;

import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskAppExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskAppMapper {
    long countByExample(FlinkTaskAppExample example);

    int deleteByExample(FlinkTaskAppExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(FlinkTaskApp record);

    int insertSelective(FlinkTaskApp record);

    List<FlinkTaskApp> selectByExampleWithBLOBs(FlinkTaskAppExample example);

    List<FlinkTaskApp> selectByExample(FlinkTaskAppExample example);

    FlinkTaskApp selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FlinkTaskApp record, @Param("example") FlinkTaskAppExample example);

    int updateByExampleWithBLOBs(@Param("record") FlinkTaskApp record, @Param("example") FlinkTaskAppExample example);

    int updateByExample(@Param("record") FlinkTaskApp record, @Param("example") FlinkTaskAppExample example);

    int updateByPrimaryKeySelective(FlinkTaskApp record);

    int updateByPrimaryKeyWithBLOBs(FlinkTaskApp record);

    int updateByPrimaryKey(FlinkTaskApp record);
}