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

package com.oppo.cloud.parser.domain.job;

import com.oppo.cloud.common.domain.gc.GCReport;
import com.oppo.cloud.common.util.textparser.ParserAction;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class SparkExecutorLogParserResult {

    /**
     * 日志路径
     */
    private String logPath;
    /**
     * 解析结果
     */
    private Map<String, ParserAction> actionMap;
    /**
     * gc报告
     */
    private List<GCReport> gcReports;
    /**
     *  异常类型
     */
    private List<String> categories;
}
