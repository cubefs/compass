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

package com.oppo.cloud.model;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskDiagnosisAdvice implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "Log type")
    private String logType;

    @ApiModelProperty(value = "Parent node exception event")
    private String parentAction;

    @ApiModelProperty(value = "Exceptional event")
    private String action;

    @ApiModelProperty(value = "Exceptional description")
    private String description;

    @ApiModelProperty(value = "Advice (variables are represented by {variable name})")
    private String variables;

    @ApiModelProperty(value = "Exception type")
    private String category;

    @ApiModelProperty(value = "Is deleted")
    private Integer deleted;

    private String normalAdvice;

    @ApiModelProperty(value = "Advice (variables are represented by {variable name})")
    private String abnormalAdvice;

    @ApiModelProperty(value = "Matching rule")
    private String rule;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getParentAction() {
        return parentAction;
    }

    public void setParentAction(String parentAction) {
        this.parentAction = parentAction;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getNormalAdvice() {
        return normalAdvice;
    }

    public void setNormalAdvice(String normalAdvice) {
        this.normalAdvice = normalAdvice;
    }

    public String getAbnormalAdvice() {
        return abnormalAdvice;
    }

    public void setAbnormalAdvice(String abnormalAdvice) {
        this.abnormalAdvice = abnormalAdvice;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", logType=").append(logType);
        sb.append(", parentAction=").append(parentAction);
        sb.append(", action=").append(action);
        sb.append(", description=").append(description);
        sb.append(", variables=").append(variables);
        sb.append(", category=").append(category);
        sb.append(", deleted=").append(deleted);
        sb.append(", normalAdvice=").append(normalAdvice);
        sb.append(", abnormalAdvice=").append(abnormalAdvice);
        sb.append(", rule=").append(rule);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }

    /**
     * 对diagnostics进行匹配诊断（针对Yarn的diagnostics信息）
     * @param diagnostics
     * @return
     */
    public String detectDiagnostics(String diagnostics) {
        String result = null;
        Pattern pattern = Pattern.compile(this.getRule(), Pattern.DOTALL);
        String[] variables = this.getVariables() == null ? new String[0] : this.getVariables().split(",");
        Matcher matcher = pattern.matcher(diagnostics);
        if (matcher.matches()) {
            String tranMsg = this.getAbnormalAdvice();
            if (variables.length != 0) {
                for (String name : variables) {
                    String value = matcher.group(name);
                    tranMsg = tranMsg.replaceAll("\\{" + name + "}", value);
                }
            }
            result = tranMsg;
        }
        return result;
    }

    /**
     * 根据es中的变量信息，生成完整的诊断建议
     * @param vars
     * @return
     * @throws Exception
     */
    public String genAdvice(Map<String, String> vars) throws Exception {
        String advice = this.getAbnormalAdvice();
        if (StringUtils.isNotEmpty(this.getVariables())) {
            for (String key : vars.keySet()) {
                String val = java.util.regex.Matcher.quoteReplacement(vars.get(key));
                try {
                    if (!val.contains("span")) {
                        advice = advice.replaceAll("\\{" + key + "}",
                                String.format("<span style=\"color: #e24a4a;\">%s</span>", val));
                    } else {
                        advice = advice.replaceAll("\\{" + key + "}", val);
                    }

                } catch (Exception e) {
                    throw new Exception(String.format("translate failed, msg:%s, advice:%s, vars:%s", e.getMessage(),
                            advice, vars));
                }
            }
        }
        return advice;
    }
}
