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

package com.oppo.cloud.flink.service;

import com.oppo.cloud.common.domain.elasticsearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisFrom;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.FlinkTaskDiagnosis;

public interface DiagnosisService {
    FlinkTaskAnalysis diagnosisApp(FlinkTaskApp flinkTaskApp, long start, long end, DiagnosisFrom from) throws Exception;

    void diagnosisAllApp(long start, long end, DiagnosisFrom from);

    void diagnosisAppHourly(long start, long end, DiagnosisFrom from);
}