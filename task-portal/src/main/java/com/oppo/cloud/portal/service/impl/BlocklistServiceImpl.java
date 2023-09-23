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

import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.model.Blocklist;
import com.oppo.cloud.model.BlocklistExample;
import com.oppo.cloud.model.Task;
import com.oppo.cloud.portal.config.ThreadLocalUserInfo;
import com.oppo.cloud.portal.dao.TaskExtendMapper;
import com.oppo.cloud.portal.domain.blocklist.BlocklistAddReq;
import com.oppo.cloud.portal.domain.blocklist.BlocklistReq;
import com.oppo.cloud.portal.domain.task.UserInfo;
import com.oppo.cloud.portal.service.BlocklistService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class BlocklistServiceImpl implements BlocklistService {

    @Autowired
    private TaskExtendMapper taskExtendMapper;

    @Autowired
    private BlocklistMapper blocklistMapper;

    /**
     * 分页查询白名单列表
     */
    @Override
    public List<Blocklist> search(BlocklistReq blocklistReq) throws Exception {
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();
        BlocklistExample blocklistExample = new BlocklistExample();
        BlocklistExample.Criteria criteria = blocklistExample.createCriteria();
        if (StringUtils.isNotBlank(blocklistReq.getComponent())) {
            criteria.andComponentEqualTo(blocklistReq.getComponent());
        }
        if (StringUtils.isNotBlank(blocklistReq.getProjectName())) {
            criteria.andProjectNameLike(blocklistReq.getProjectName());
        }
        if (StringUtils.isNotBlank(blocklistReq.getFlowName())) {
            criteria.andFlowNameLike(blocklistReq.getFlowName());
        }
        if (StringUtils.isNotBlank(blocklistReq.getTaskName())) {
            criteria.andTaskNameLike(blocklistReq.getTaskName());
        }
        criteria.andDeletedEqualTo(0);
        if (!userInfo.isAdmin()) {
            criteria.andUsernameEqualTo(userInfo.getUsername());
        }
        return blocklistMapper.selectByExample(blocklistExample);
    }

    /**
     * 批量删除任务白名单
     */
    @Override
    public void deleteByIds(List<Integer> blocklistIds) throws Exception {
        for (Integer id : blocklistIds) {
            Blocklist blocklist = blocklistMapper.selectByPrimaryKey(id);
            blocklist.setDeleted(1);
            blocklist.setUpdateTime(new Date());
            blocklistMapper.updateByPrimaryKey(blocklist);
        }
    }

    /**
     * 任务查询
     */
    @Override
    public List<Task> searchTasks(BlocklistAddReq blocklistAddReq) throws Exception {
        if (StringUtils.isAllBlank(blocklistAddReq.getProjectName(), blocklistAddReq.getFlowName(), blocklistAddReq.getTaskName())) {
            return null;
        }
        return taskExtendMapper.searchTasksLike(blocklistAddReq.getProjectName(), blocklistAddReq.getFlowName(),
                blocklistAddReq.getTaskName());
    }

    /**
     * 添加任务白名单
     */
    @Override
    public void addBlocklist(BlocklistAddReq blocklistAddReq) throws Exception {
        UserInfo userInfo = ThreadLocalUserInfo.getCurrentUser();

        // 查询白名单是否已经存在
        BlocklistExample blocklistExample = new BlocklistExample();
        BlocklistExample.Criteria criteria = blocklistExample.createCriteria()
                .andTaskNameEqualTo(blocklistAddReq.getTaskName())
                .andFlowNameEqualTo(blocklistAddReq.getFlowName())
                .andProjectNameEqualTo(blocklistAddReq.getProjectName());

        if (blocklistAddReq.getComponent() != null) {
            criteria.andComponentEqualTo(blocklistAddReq.getComponent());
        }

        List<Blocklist> blocklists = blocklistMapper.selectByExample(blocklistExample);
        if (blocklists.size() == 0) {
            Blocklist blocklist = new Blocklist();
            BeanUtils.copyProperties(blocklistAddReq, blocklist);
            if (blocklistAddReq.getComponent() != null) {
                blocklist.setComponent(blocklistAddReq.getComponent());
            } else {
                blocklist.setComponent("offline");
            }
            blocklist.setUpdateTime(new Date());
            blocklist.setCreateTime(new Date());
            blocklist.setDeleted(0);
            blocklist.setUsername(userInfo.getUsername());

            blocklistMapper.insert(blocklist);

        } else {
            blocklists.forEach(data -> {
                data.setDeleted(0);
                blocklistMapper.updateByPrimaryKey(data);
            });
        }

    }

}
