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

package com.oppo.cloud.portal.domain.app;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class AppDiagnosisMetadata {
    @NotBlank(message = "applicationId must not be blank")
    private String applicationId;

    @NotBlank(message = "applicationType must not be blank")
    private String applicationType;

    @NotNull(message = "vcoreSeconds must not be null")
    private Double vcoreSeconds;

    @NotNull(message = "memorySeconds must not be null")
    private Double memorySeconds;

    @NotNull(message = "startedTime must not be null")
    private Long startedTime;

    @NotNull(message = "finishedTime must not be null")
    private Long finishedTime;

    @NotNull(message = "elapsedTime must not be null")
    private Double elapsedTime;

    @NotNull(message = "amHostHttpAddress must not be null")
    private String amHostHttpAddress;

    private String sparkEventLogFile;

    private String sparkExecutorLogDirectory;

    private String mapreduceEventLogDirectory;

    private String mapreduceContainerLogDirectory;

    private String diagnostics;

    private String queue;

    private String user;

    private String clusterName;

}
