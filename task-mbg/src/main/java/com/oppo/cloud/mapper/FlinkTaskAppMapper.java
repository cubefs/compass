package com.oppo.cloud.mapper;

import com.oppo.cloud.model.RealtimeTaskApp;
import com.oppo.cloud.model.RealtimeTaskAppExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlinkTaskAppMapper {
    long countByExample(RealtimeTaskAppExample example);

    int deleteByExample(RealtimeTaskAppExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RealtimeTaskApp record);

    int insertSelective(RealtimeTaskApp record);

    List<RealtimeTaskApp> selectByExampleWithBLOBs(RealtimeTaskAppExample example);

    List<RealtimeTaskApp> selectByExample(RealtimeTaskAppExample example);

    RealtimeTaskApp selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RealtimeTaskApp record, @Param("example") RealtimeTaskAppExample example);

    int updateByExampleWithBLOBs(@Param("record") RealtimeTaskApp record, @Param("example") RealtimeTaskAppExample example);

    int updateByExample(@Param("record") RealtimeTaskApp record, @Param("example") RealtimeTaskAppExample example);

    int updateByPrimaryKeySelective(RealtimeTaskApp record);

    int updateByPrimaryKeyWithBLOBs(RealtimeTaskApp record);

    int updateByPrimaryKey(RealtimeTaskApp record);
}