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

package com.oppo.cloud.gpt.config;

import com.oppo.cloud.gpt.drain.MaskRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "drain")
public class DrainConfig {
    /**
     * mask for log message with regex and mask name
     */
    private List<MaskRule> maskRules;
    /**
     * prefix for each mask
     */
    private String maskPrefix;
    /**
     * suffix for each mask
     */
    private String maskSuffix;
    /**
     * Percentage of similar tokens for a log message is below this threshold,
     * a new log cluster will be created.
     */
    private Double similarityThreshold;
    /**
     * Maximum depth levels of log clusters(Minimum is 2).
     * For example, for depth is 2, root is considered depth level 1.
     * Token count is considered depth level 2.
     * First log token is considered depth level 3...
     */
    private Integer maxDepth;
    /**
     * Maximum number of children of an internal node
     */
    private Integer maxChildren;
    /**
     * Maximum number of log cluster, -1 is unlimited
     */
    private Integer maxClusters;
    /**
     * Delimiters to apply when splitting log message into words, default: space.
     */
    private String[] delimiters;
    /**
     * Maximum tokens to aggregate log cluster. -1 is unlimited.
     */
    private Integer maxTokens;
}
