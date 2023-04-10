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

package com.oppo.cloud.portal.controller;

import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.api.CommonPage;

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.model.Blocklist;
import com.oppo.cloud.portal.domain.blocklist.BlocklistAddReq;
import com.oppo.cloud.portal.domain.blocklist.BlocklistDelReq;
import com.oppo.cloud.portal.domain.blocklist.BlocklistReq;
import com.oppo.cloud.portal.service.BlocklistService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 白名单功能controller
 */
@Controller
@RequestMapping(value = "/api/v1/blocklist")
@Api(value = "AbnormalTaskController", description = "白名单接口")
@Slf4j
public class BlocklistController {

    @Autowired
    BlocklistService blocklistService;

    @PostMapping(value = "/list")
    @ApiOperation(value = "白名单列表信息", httpMethod = "POST")
    @ResponseBody
    public CommonStatus list(@RequestBody BlocklistReq blocklistReq) throws Exception {
        PageHelper.startPage(blocklistReq.getPage(), blocklistReq.getPageSize());
        List<Blocklist> blocklists = blocklistService.search(blocklistReq);
        return CommonStatus.success(CommonPage.restPage(blocklists));
    }

    @PostMapping(value = "/del")
    @ApiOperation(value = "删除白名单", httpMethod = "POST")
    @ResponseBody
    public CommonStatus delete(@RequestBody BlocklistDelReq blocklistDelReq) throws Exception {
        blocklistService.deleteByIds(blocklistDelReq.getBlocklistIds());
        return CommonStatus.success("ok");
    }

    @PostMapping(value = "/searchTasks")
    @ApiOperation(value = "添加白名单", httpMethod = "POST")
    @ResponseBody
    @Transactional
    public CommonStatus searchTasks(@RequestBody BlocklistAddReq blocklistAddReq) throws Exception {
        return CommonStatus.success(blocklistService.searchTasks(blocklistAddReq));
    }

    @PostMapping(value = "/add")
    @ApiOperation(value = "添加白名单", httpMethod = "POST")
    @ResponseBody
    @Transactional
    public CommonStatus add(@RequestBody @Valid BlocklistAddReq blocklistAddReq) throws Exception {
        blocklistService.addBlocklist(blocklistAddReq);
        return CommonStatus.success("ok");
    }

}
