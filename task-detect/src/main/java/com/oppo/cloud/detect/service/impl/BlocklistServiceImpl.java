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

package com.oppo.cloud.detect.service.impl;

import com.oppo.cloud.detect.service.BlocklistService;
import com.oppo.cloud.mapper.BlocklistMapper;
import com.oppo.cloud.model.Blocklist;
import com.oppo.cloud.model.BlocklistExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Block list
 */
@Slf4j
@Service
public class BlocklistServiceImpl implements BlocklistService {

    @Autowired
    private BlocklistMapper blocklistMapper;

    /**
     * Check if the task to be detected is in the block list.
     */
    @Override
    public Boolean isBlocklistTask(String projectName, String flowName, String taskName) {
        BlocklistExample blocklistExample = new BlocklistExample();
        BlocklistExample.Criteria criteria = blocklistExample.createCriteria();
        criteria.andTaskNameEqualTo(taskName).andDeletedEqualTo(0);
        if (StringUtils.isNotBlank(projectName)) {
            criteria.andProjectNameEqualTo(projectName);
        }
        if (StringUtils.isNotBlank(flowName)) {
            criteria.andFlowNameEqualTo(flowName);
        }
        List<Blocklist> blocklists = blocklistMapper.selectByExample(blocklistExample);
        return blocklists.size() > 0;
    }
}
