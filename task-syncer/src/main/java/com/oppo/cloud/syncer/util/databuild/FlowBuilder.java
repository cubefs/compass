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

import com.oppo.cloud.model.Flow;
import com.oppo.cloud.syncer.util.DataUtil;

import java.util.Map;

/**
 * Dag流定义
 */
public class FlowBuilder implements DataBuilder<Flow> {

    @Override
    public Flow run(Map<String, String> data) {
        Flow flow = new Flow();
        flow.setId(DataUtil.parseInteger(data.get("id")));
        flow.setName(data.get("flow_name"));
        flow.setDescription(data.get("description"));
        flow.setUserId(DataUtil.parseInteger(data.get("user_id")));
        flow.setStatus(DataUtil.parseInteger(data.get("flow_status")));
        flow.setProjectName(data.get("project_name"));
        flow.setProjectId(DataUtil.parseInteger(data.get("project_id")));
        flow.setCreateTime(DataUtil.parseDate(data.get("create_time")));
        flow.setUpdateTime(DataUtil.parseDate(data.get("update_time")));
        return flow;
    }
}
