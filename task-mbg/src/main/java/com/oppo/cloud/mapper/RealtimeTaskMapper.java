package com.oppo.cloud.mapper;

import com.oppo.cloud.model.RealtimeTask;
import com.oppo.cloud.model.RealtimeTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface RealtimeTaskMapper {
    long countByExample(RealtimeTaskExample example);

    int deleteByExample(RealtimeTaskExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RealtimeTask record);

    int insertSelective(RealtimeTask record);

    List<RealtimeTask> selectByExample(RealtimeTaskExample example);

    RealtimeTask selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RealtimeTask record, @Param("example") RealtimeTaskExample example);

    int updateByExample(@Param("record") RealtimeTask record, @Param("example") RealtimeTaskExample example);

    int updateByPrimaryKeySelective(RealtimeTask record);

    int updateByPrimaryKey(RealtimeTask record);
}