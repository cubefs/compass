package com.oppo.cloud.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

public class RealtimeTaskDiagnosisRuleAdvice implements Serializable {
    @ApiModelProperty(value = "实时任务诊断规则结果id")
    private Integer id;

    @ApiModelProperty(value = "实时任务诊断id")
    private Integer realtimeTaskDiagnosisId;

    @ApiModelProperty(value = "规则名")
    private String ruleName;

    @ApiModelProperty(value = "规则编码")
    private Integer ruleType;

    @ApiModelProperty(value = "规则是否命中0未1有")
    private Short hasAdvice;

    @ApiModelProperty(value = "诊断规则描述")
    private String description;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRealtimeTaskDiagnosisId() {
        return realtimeTaskDiagnosisId;
    }

    public void setRealtimeTaskDiagnosisId(Integer realtimeTaskDiagnosisId) {
        this.realtimeTaskDiagnosisId = realtimeTaskDiagnosisId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Integer getRuleType() {
        return ruleType;
    }

    public void setRuleType(Integer ruleType) {
        this.ruleType = ruleType;
    }

    public Short getHasAdvice() {
        return hasAdvice;
    }

    public void setHasAdvice(Short hasAdvice) {
        this.hasAdvice = hasAdvice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", realtimeTaskDiagnosisId=").append(realtimeTaskDiagnosisId);
        sb.append(", ruleName=").append(ruleName);
        sb.append(", ruleType=").append(ruleType);
        sb.append(", hasAdvice=").append(hasAdvice);
        sb.append(", description=").append(description);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}