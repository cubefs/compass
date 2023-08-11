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

package com.oppo.cloud.flink.advice.turning;

import com.google.common.collect.Lists;
import com.oppo.cloud.flink.advice.turning.mem.TurningMemDownBySpec;
import com.oppo.cloud.flink.advice.turning.mem.TurningMemDownStrategy;
import com.oppo.cloud.flink.advice.turning.mem.TurningMemUpBySpec;
import com.oppo.cloud.flink.advice.turning.mem.TurningMemUpStrategy;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisTurningStatus;
import com.oppo.cloud.flink.domain.diagnosis.DiagnosisContext;
import com.oppo.cloud.flink.util.DoctorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TurningManager {

    @Autowired
    TurningCpuDownByParallel turningCpuDownByParallel;

    @Autowired
    TurningCpuUpByParallel turningCpuUpByParallel;

    @Autowired
    TurningCpuUpBySpec turningCpuUpBySpec;

    @Autowired
    TurningCpuDownBySpec turningCpuDownBySpec;

    @Autowired
    TurningMemDownBySpec turningMemDownBySpec;

    @Autowired
    TurningMemUpBySpec turningMemUpBySpec;

    @Autowired
    DoctorUtil doctorUtil;

    // 调大cpu
    public TurningAdvice turningCpuUp(DiagnosisContext context) {
        List<TurningCpuUpStrategy> strategies = Lists.newArrayList(turningCpuUpByParallel, turningCpuUpBySpec
        );
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningCpuUpStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription())
                            + turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }

    // 调小cpu
    public TurningAdvice turningCpuDown(DiagnosisContext context) {
        List<TurningCpuDownStrategy> strategies = Lists.newArrayList(turningCpuDownByParallel, turningCpuDownBySpec
        );
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningCpuDownStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription())
                            + turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }

    // 调小内存
    public TurningAdvice turningMemDown(DiagnosisContext context) {
        List<TurningMemDownStrategy> strategies = Lists.newArrayList(turningMemDownBySpec);
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningMemDownStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription()) +
                            turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }
    // 调大内存
    public TurningAdvice turningMemUp(DiagnosisContext context) {
        List<TurningMemUpStrategy> strategies = Lists.newArrayList(turningMemUpBySpec);
        TurningAdvice noAdviceRes = new TurningAdvice();
        for (TurningMemUpStrategy s : strategies) {
            try {
                TurningAdvice turning = s.turning(context);
                if (turning != null && turning.getStatus() == DiagnosisTurningStatus.HAS_ADVICE) {
                    return turning;
                } else if (turning != null) {
                    noAdviceRes.setDescription(doctorUtil.addComma(noAdviceRes.getDescription()) +
                            turning.getDescription());
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        return noAdviceRes;
    }
}
