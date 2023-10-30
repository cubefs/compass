package com.oppo.cloud.mapper;

import com.oppo.cloud.model.Flow;
import com.oppo.cloud.model.FlowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FlowMapper {
    long countByExample(FlowExample example);

    int deleteByExample(FlowExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Flow record);

    int insertSelective(Flow record);

    List<Flow> selectByExample(FlowExample example);

    Flow selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Flow record, @Param("example") FlowExample example);

    int updateByExample(@Param("record") Flow record, @Param("example") FlowExample example);

    int updateByPrimaryKeySelective(Flow record);

    int updateByPrimaryKey(Flow record);
}