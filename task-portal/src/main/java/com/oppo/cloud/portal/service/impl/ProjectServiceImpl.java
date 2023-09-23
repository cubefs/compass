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

package com.oppo.cloud.portal.service.impl;

import com.oppo.cloud.mapper.ProjectMapper;
import com.oppo.cloud.model.Project;
import com.oppo.cloud.model.ProjectExample;
import com.oppo.cloud.portal.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目列表查询服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<Project> getAllProject(Integer userId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andUserIdEqualTo(userId);
        return projectMapper.selectByExample(projectExample);
    }

    @Override
    public List<Project> getAllProject() {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria();
        return projectMapper.selectByExample(projectExample);
    }

    @Override
    public List<String> getProjectNames(Integer userId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andUserIdEqualTo(userId);
        List<Project> projectList = projectMapper.selectByExample(new ProjectExample());
        return projectList.stream().map(Project::getProjectName).collect(Collectors.toList());
    }
}
