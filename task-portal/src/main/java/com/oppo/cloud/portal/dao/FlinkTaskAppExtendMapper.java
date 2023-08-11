package com.oppo.cloud.portal.dao;

import com.oppo.cloud.mapper.FlinkTaskAppMapper;
import com.oppo.cloud.model.FlinkTaskApp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlinkTaskAppExtendMapper extends FlinkTaskAppMapper {

    /**
     * 批量保存
     * @param apps
     */
    void batchSave(@Param("apps") List<FlinkTaskApp> apps);
}
