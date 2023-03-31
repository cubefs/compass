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

package com.oppo.cloud.syncer.util.databuild;

import com.oppo.cloud.model.Project;
import com.oppo.cloud.syncer.util.DataUtil;

import java.util.Map;

/**
 * 项目数据构建
 */
public class ProjectBuilder implements DataBuilder<Project> {

    @Override
    public Project run(Map<String, String> data) {
        Project project = new Project();
        project.setId(DataUtil.parseInteger(data.get("id")));
        project.setProjectName(data.get("project_name"));
        project.setProjectStatus(DataUtil.parseInteger(data.get("project_status")));
        project.setUserId(DataUtil.parseInteger(data.get("user_id")));
        project.setDescription(data.get("description"));
        project.setCreateTime(DataUtil.parseDate(data.get("create_time")));
        project.setUpdateTime(DataUtil.parseDate(data.get("update_time")));
        return project;
    }
}
