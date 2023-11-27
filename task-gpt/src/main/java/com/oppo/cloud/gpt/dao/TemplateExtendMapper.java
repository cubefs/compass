package com.oppo.cloud.gpt.dao;

import com.oppo.cloud.mapper.TemplateMapper;
import com.oppo.cloud.model.Template;
import org.apache.ibatis.annotations.Insert;

/**
 * Template Mapper
 */
public interface TemplateExtendMapper extends TemplateMapper {

    /**
     * insert a record
     * compatible with mysql and postgresql
     * @param template
     */
    @Insert("insert into template (cid, update_time, create_time, cluster, raw_log) " +
            "values (#{cid,jdbcType=VARCHAR}, #{updateTime,jdbcType=TIMESTAMP}, #{createTime,jdbcType=TIMESTAMP}, #{cluster,jdbcType=LONGVARCHAR}, #{rawLog,jdbcType=LONGVARCHAR})")
    void save(Template template);
}
