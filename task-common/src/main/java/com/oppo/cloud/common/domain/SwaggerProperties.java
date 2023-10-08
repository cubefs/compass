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

package com.oppo.cloud.common.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Swagger Configuration
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class SwaggerProperties {

    /**
     * API base url path
     */
    private String apiBasePackage;
    /**
     * Enable security
     */
    private boolean enableSecurity;
    /**
     * Document title
     */
    private String title;
    /**
     * Document description
     */
    private String description;
    /**
     * Document version
     */
    private String version;
    /**
     * Document contact
     */
    private String contactName;
    /**
     * Document contact url
     */
    private String contactUrl;
    /**
     * Document contact email
     */
    private String contactEmail;
}
