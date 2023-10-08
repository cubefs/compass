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
 * Single matching rule
 */
@Data
@JsonIgnoreProperties
public class ParserAction {

    /**
     * Action or Activity
     */
    private String action;

    /**
     * Parent node action (used when reading MySQL configuration)
     */
    private String parentAction;

    /**
     * Description
     */
    private String desc;

    /**
     * Diagnosis type
     */
    private String category;

    /**
     * Step
     */
    private int step;

    /**
     * Whether to skip
     */
    private boolean skip;

    /**
     * Parsing method：
     * DEFAULT: Line or block matching
     * JOIN Merge the results into one line before matching
     */
    private ParserType parserType;

    /**
     * Text parsing template: beginning, middle, and end lines.
     */
    private ParserTemplate parserTemplate;

    /**
     * Name of the captured group.
     */
    private String[] groupNames;

    /**
     *  Group matching data
     */
    private Map<String, String> groupData;

    /**
     * Whether the match is successful or not
     */
    private boolean matchSucceed;

    /**
     * Matched results
     */
    private List<ParserResult> parserResults;

    /**
     * Root node result
     */
    private List<ParserResult> rootResults;

    /**
     * Hash code of the matching result
     */
    private Set<Integer> hashCode;

    /**
     * Child node rules
     */
    private List<ParserAction> children;
}
