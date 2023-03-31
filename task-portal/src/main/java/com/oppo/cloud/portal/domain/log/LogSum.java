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

package com.oppo.cloud.portal.domain.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@ApiModel("日志解析数据结构")
public class LogSum implements Comparable<LogSum> {

    @JsonIgnore
    private String id;

    @ApiModelProperty(value = "app Id", name = "applicationId")
    private String applicationId;

    @ApiModelProperty(value = "任务名称", name = "taskName")
    private String taskName;

    @ApiModelProperty(value = "流程名称", name = "flowName")
    private String flowName;

    @ApiModelProperty(value = "项目名称", name = "projectName")
    private String projectName;

    @ApiModelProperty(value = "execution Date", name = "executionTime")
    private Integer executionTime;

    @ApiModelProperty(value = "retry Number", name = "tryNum")
    private Integer tryNum;

    @ApiModelProperty(value = "处理动作", name = "action")
    private String action;

    @ApiModelProperty(value = "描述", name = "desc")
    private String desc;

    @ApiModelProperty(value = "日志类型", name = "logType")
    private String logType;

    @ApiModelProperty(value = "匹配规则", name = "rule")
    private String rule;

    @ApiModelProperty(value = "原始日志", name = "rawLog")
    private String rawLog;

    @Field(value = "vars", type = FieldType.Object)
    private Object vars;

    @ApiModelProperty(value = "日志路径", name = "logPath")
    private String logPath;

    @ApiModelProperty(value = "日志路径列表", name = "logPaths")
    private List<String> logPaths;

    @ApiModelProperty(value = "ocs/s3 的key", name = "accessKey")
    private String accessKey;

    @ApiModelProperty(value = "时间戳", name = "timestamp")
    private Integer timestamp;

    @ApiModelProperty(value = "匹配步骤", name = "step")
    private String step;

    @ApiModelProperty(value = "诊断建议", name = "diagnoseAdvice")
    private String diagnoseAdvice;

    @ApiModelProperty(value = "sparkUi链接", name = "sparkUI")
    private String sparkUI;

    @Override
    public int compareTo(LogSum object) {
        // 按诊断建议
        if ((StringUtils.isNotBlank(this.diagnoseAdvice) && StringUtils.isNotBlank((object).getDiagnoseAdvice())) ||
                (StringUtils.isBlank(this.diagnoseAdvice) && StringUtils.isBlank((object).getDiagnoseAdvice()))) {
            if (this.timestamp != null && (object).getTimestamp() != null
                    && !this.timestamp.equals((object).getTimestamp())) {
                // 按时间
                return this.timestamp.compareTo((object).getTimestamp());
            } else if (this.step != null && (object).getStep() != null) {
                // 按步骤
                return this.step.compareTo((object).getStep());
            }
        } else if (StringUtils.isNotBlank(this.diagnoseAdvice) && StringUtils.isBlank((object).getDiagnoseAdvice())) {
            // 有建议的放在前面
            return -1;
        } else if (StringUtils.isBlank(this.diagnoseAdvice) && StringUtils.isNotBlank((object).getDiagnoseAdvice())) {
            return 1;
        }
        return 0;
    }

    public String formatRawLog() {
        String oflowLogTime = "\\[(?<datetime>\\d{4}.\\d{2}.\\d{2}\\D+\\d{2}:\\d{2}:\\d{2}),\\d+\\] " +
                "\\{bash_operator.py:\\d+\\} INFO (- )?(\\d{2}.\\d{2}.\\d{2} \\d{2}:\\d{2}:\\d{2} )?";
        String driverAndDriverLogTime = "^(?<datetime>\\d{2}/\\d{2}/\\d{2}\\D+\\d{2}:\\d{2}:\\d{2}) (INFO|ERROR) ";
        rawLog = rawLog.replaceAll("\n", "<br/>");
        // rawLog = rawLog.replaceAll(ansiTrim, ""); //去掉颜色字符
        switch (logType) {
            case "oflow":
                rawLog = rawLog.replaceAll(oflowLogTime, "");
                break;
            case "driver":
            case "executor":
                rawLog = rawLog.replaceAll(driverAndDriverLogTime, "");
                break;
        }
        // rawLog = StringUtils.strip(StringUtils.strip(rawLog, "\u001B[1;31m"), "\u001B[m");
        rawLog = rawLog.replaceAll("\u001B\\[.*?m", "");
        return rawLog;
    }
}
