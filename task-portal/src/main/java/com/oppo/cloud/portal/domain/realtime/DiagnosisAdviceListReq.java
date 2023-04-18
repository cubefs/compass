package com.oppo.cloud.portal.domain.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("实时任务诊断结果分页查询")
public class DiagnosisAdviceListReq {
    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "诊断开始时间")
    private LocalDateTime start;

    @ApiModelProperty(value = "诊断结束时间")
    private LocalDateTime end;

    @ApiModelProperty(value = "创建者")
    private String username;

    @ApiModelProperty(value = "app id")
    private String applicationId;

    @ApiModelProperty("排序列")
    private String orderColumn;
    @ApiModelProperty("排序顺序")
    private String orderType;
    @ApiModelProperty("包括规则")
    private List<Integer> diagnosisRule;
    @ApiModelProperty("排除规则")
    private List<Integer> diagnosisRuleNe;
    @ApiModelProperty("包括资源类型")
    private List<Integer> resourceDiagnosisType;
    @ApiModelProperty("排除资源类型")
    private List<Integer> resourceDiagnosisTypeNe;
    @ApiModelProperty("诊断来源")
    private List<Integer> diagnosisFrom;

    @ApiModelProperty(value = "页码")
    @Min(value = 1, message = "page 不能小于1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量")
    @Max(value = 500, message = "pageSize 不能大于500")
    private Integer pageSize = 15;
}
