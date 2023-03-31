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

package com.oppo.cloud.common.util.textparser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 单个匹配规则
 */
@Data
@JsonIgnoreProperties
public class ParserAction {

    /**
     * 动作
     */
    private String action;

    /**
     * 父节点action(读取mysql配置时使用)
     */
    private String parentAction;

    /**
     * 描述
     */
    private String desc;

    /**
     * 诊断类型
     */
    private String category;

    /**
     * 步骤
     */
    private int step;

    /**
     * 是否跳过
     */
    private boolean skip;

    /**
     * 解析方式：
     * DEFAULT: 行或块匹配
     * JOIN 把结果合并成一行再匹配
     */
    private ParserType parserType;

    /**
     * 文本解析模板：开头，中间和结束行
     */
    private ParserTemplate parserTemplate;

    /**
     * 分组匹配名称
     */
    private String[] groupNames;

    /**
     * 分组匹配数据
     */
    private Map<String, String> groupData;

    /**
     * 是否匹配成功
     */
    private boolean matchSucceed;

    /**
     * 匹配结果
     */
    private List<ParserResult> parserResults;

    /**
     * 根节点结果
     */
    private List<ParserResult> rootResults;

    /**
     * 匹配结果hashCode
     */
    private Set<Integer> hashCode;

    /**
     * 子节点规则
     */
    private List<ParserAction> children;
}
