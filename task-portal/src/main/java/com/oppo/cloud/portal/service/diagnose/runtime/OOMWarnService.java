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

package com.oppo.cloud.portal.service.diagnose.runtime;

import com.alibaba.fastjson2.JSONObject;
import com.oppo.cloud.common.constant.AppCategoryEnum;
import com.oppo.cloud.common.domain.eventlog.*;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.model.TaskDiagnosisAdvice;
import com.oppo.cloud.model.TaskDiagnosisAdviceExample;
import com.oppo.cloud.portal.domain.base.Conclusion;
import com.oppo.cloud.portal.domain.diagnose.IsAbnormal;
import com.oppo.cloud.portal.domain.diagnose.Table;
import com.oppo.cloud.portal.domain.diagnose.runtime.OOMWarn;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OOMWarnService extends RunTimeBaseService<OOMWarn> {

    @Override
    public String getCategory() {
        return AppCategoryEnum.OOMWarn.getCategory();
    }

    @Override
    public OOMWarn generateData(DetectorResult detectorResult, DetectorConfig config) throws Exception {
        OOMAbnormal oomAbnormal = ((JSONObject) detectorResult.getData()).toJavaObject(OOMAbnormal.class);
        OOMWarn oomWarn = new OOMWarn();
        oomWarn.setAbnormal(detectorResult.getAbnormal());
        if (!detectorResult.getAbnormal()) {
            return null;
        }
        Table<OOMWarn.BoardCastTable> boardCastTableTable = oomWarn.getTable();
        List<OOMWarn.BoardCastTable> boardCastTableList = boardCastTableTable.getData();
        for (OOMTableInfo oomTableInfo : oomAbnormal.getTables()) {
            OOMWarn.BoardCastTable boardCastTable = new OOMWarn.BoardCastTable();
            boardCastTable.setHiveTable(oomTableInfo.getTable());
            boardCastTable.setOutputOfColumns(UnitUtil.transferRows(oomTableInfo.getRows()));
            boardCastTable.setMemoryUsed(UnitUtil.transferByte(oomTableInfo.getMemory()));
            boardCastTableList.add(boardCastTable);
        }
        oomWarn.setVars(oomAbnormal.getVars());
        oomWarn.getVars().put("broadcastRows", UnitUtil.transferRows(config.getOomWarnConfig().getBroadcastRows()));
        oomWarn.getVars().put("broadcastRowsOom", String.valueOf(config.getOomWarnConfig().getBroadcastRowsOom()));
        oomWarn.getVars().put("oom", String.valueOf(config.getOomWarnConfig().getOom()));
        oomWarn.getVars().put("maxRows", UnitUtil.transferRows(Long.parseLong(oomWarn.getVars().get("maxRows"))));

        return oomWarn;
    }

    @Override
    public String generateConclusionDesc(Map<String, String> thresholdMap) {
        return String.format("广播表的累计内存与driver或executor任意一个内存占比超过%s%%，即判定为OOM预警", "40");
    }

    @Override
    public String generateItemDesc() {
        return "OOM预警分析";
    }

    @Override
    public String getType() {
        return "table";
    }

    /**
     * 这个异常类型有两种建议，需要重写父类的方法
     *
     * @param isAbnormal
     * @return
     */
    @Override
    public Conclusion generateConclusion(IsAbnormal isAbnormal) {
        if (isAbnormal == null) {
            return null;
        }
        String advice = "";
        String action = isAbnormal.getVars().get("action");
        TaskDiagnosisAdviceExample diagnoseAdviceExample = new TaskDiagnosisAdviceExample();
        diagnoseAdviceExample.createCriteria().andCategoryEqualTo(this.getCategory());
        List<TaskDiagnosisAdvice> diagnoseAdviceList =
                diagnoseAdviceMapper.selectByExampleWithBLOBs(diagnoseAdviceExample);
        for (TaskDiagnosisAdvice diagnoseAdvice : diagnoseAdviceList) {
            if (diagnoseAdvice.getAction().equals(action)) {
                advice = diagnoseAdvice.getNormalAdvice();
                try {
                    if (isAbnormal.getAbnormal()) {
                        advice = diagnoseAdvice.genAdvice(isAbnormal.getVars());
                    } else {
                        advice = diagnoseAdvice.getNormalAdvice();
                    }
                } catch (Exception e) {
                    log.error("formatAdvice failed, action:{},vars:{}, msg:{}", diagnoseAdvice.getAction(),
                            isAbnormal.getVars(), e.getMessage());
                }
            }
        }
        return new Conclusion(advice, this.generateConclusionDesc(isAbnormal.getVars()));
    }

}
