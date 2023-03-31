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

package com.oppo.cloud.common.domain.cluster.spark;

import com.oppo.cloud.common.domain.cluster.yarn.Attempt;
import lombok.Data;

import java.util.List;

/**
 * sparK rest api app 重试
 */
@Data
public class SparkApplication {

    /**
     * spark app id
     */
    private String id;
    /**
     * spark应用名称
     */
    private String name;
    /**
     * 重试列表
     */
    private List<Attempt> attempts;
}
