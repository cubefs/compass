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

import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.portal.domain.app.AppDiagnosisMetadata;
import com.oppo.cloud.portal.service.LogRecordService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Slf4j
@Controller
public class LogRecordController {

    @Autowired
    private LogRecordService logRecordService;

    @PostMapping("/openapi/offline/app/metadata")
    @ApiOperation(value = "report spark, may include scheduler information")
    @ResponseBody
    public CommonStatus<?> reportLogRecord(@RequestBody @Valid AppDiagnosisMetadata appInfo) throws Exception {
        logRecordService.reportLogRecord(appInfo);
        return CommonStatus.success("ok");
    }


}
