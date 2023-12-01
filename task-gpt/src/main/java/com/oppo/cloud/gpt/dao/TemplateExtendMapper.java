/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
