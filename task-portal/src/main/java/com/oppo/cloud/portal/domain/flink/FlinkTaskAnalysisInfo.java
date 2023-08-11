package com.oppo.cloud.portal.domain.flink;

import com.oppo.cloud.common.domain.elasticsearch.FlinkTaskAdvice;
import com.oppo.cloud.common.domain.elasticsearch.FlinkTaskAnalysis;
import com.oppo.cloud.common.domain.elasticsearch.SimpleUser;
import com.oppo.cloud.common.util.DateUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@ApiModel(value = "flink task web 信息结构")
public class FlinkTaskAnalysisInfo {

    @ApiModelProperty(value = "记录id")
    private String id;

    @ApiModelProperty(value = "yarn applicationId")
    private String applicationId;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "工作流名称")
    private String flowName;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "作业名称")
    private String jobName;

    @ApiModelProperty(value = "运行开始时间")
    private String startTime;

    @ApiModelProperty(value = "时间消耗")
    private String timeCost;

    @ApiModelProperty(value = "队列*")
    private String queue;

    @ApiModelProperty(value = "并行度*")
    private Integer parallel;

    /* flink slot */
    private Integer tmSlot;

    /* flink task manager core */
    private Integer tmCore;

    /* flink task manager memory */
    private Integer tmMemory;

    /* flink job manager memory */
    private Integer jmMemory;


    /* flink task manager num */
    private Integer tmNum;

    @ApiModelProperty(value = "资源消耗")
    private String resourceCost;

    @ApiModelProperty(value = "创建人")
    private String username;

    @ApiModelProperty(value = "内存资源消耗")
    private String memCost;

    @ApiModelProperty(value = "诊断类型")
    private List<String> ruleNames;

    @ApiModelProperty(value = "flink track url")
    private String flinkTrackUrl;

    @ApiModelProperty(value = "诊断建议")
    private String resourceAdvice;

    @ApiModelProperty(value = "开始诊断时间")
    private String diagnosisStartTime;

    @ApiModelProperty(value = "结束诊断时间")
    private String diagnosisEndTime;

    /* 建议并行度 */
    private Integer diagnosisParallel;

    /* 建议job manager 内存大小单位MB */
    private Integer diagnosisJmMemory;

    /* 建议task manager 内存大小单位MB */
    private Integer diagnosisTmMemory;

    /* 建议tm的slot数量 */
    private Integer diagnosisTmSlotNum;

    /* 建议tm的core数量 */
    private Integer diagnosisTmCoreNum;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    /**
     * 换行格式
     *
     * @param item
     * @return
     */
    public static FlinkTaskAnalysisInfo from(FlinkTaskAnalysis item) {
        FlinkTaskAnalysisInfo info = new FlinkTaskAnalysisInfo();
        // basic info
        info.setId(item.getDocId());
        info.setApplicationId(item.getApplicationId());
        info.setProjectName(item.getProjectName());
        info.setFlowName(item.getFlowName());
        info.setTaskName(item.getTaskName());
        info.setJobName(item.getJobName());
        info.setQueue(item.getQueue());
        info.setParallel(item.getParallel());
        info.setFlinkTrackUrl(item.getFlinkTrackUrl());
        info.setTmCore(item.getTmCore());
        info.setTmMemory(item.getTmMemory());
        info.setJmMemory(item.getJmMemory());
        info.setTmSlot(item.getTmSlot());
        info.setTmNum(item.getTmNum());
        info.setRuleNames(item.getDiagnosisTypes());
        info.setCreateTime(DateUtil.format(item.getCreateTime()));
        info.setUpdateTime(DateUtil.format(item.getUpdateTime()));
        info.setUsername(item.getUsers().stream().map(SimpleUser::getUsername).collect(Collectors.joining(",")));
        info.setStartTime(DateUtil.format(item.getStartTime()));
        info.setDiagnosisStartTime(DateUtil.format(item.getDiagnosisStartTime()));
        info.setDiagnosisEndTime(DateUtil.format(item.getDiagnosisEndTime()));
        info.setDiagnosisJmMemory(item.getDiagnosisJmMemory());
        info.setDiagnosisTmMemory(item.getDiagnosisTmMemory());
        info.setDiagnosisParallel(item.getDiagnosisParallel());
        info.setDiagnosisTmCoreNum(item.getDiagnosisTmCoreNum());
        info.setDiagnosisTmSlotNum(item.getDiagnosisTmSlotNum());

        // cost
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.ofEpochSecond(item.getStartTime().getTime() / 1000, 0, ZoneOffset.ofHours(8));

        Duration duration = Duration.between(start, now);
        String timeCost = UnitUtil.transferTimeUnit(duration.getSeconds() * 1000);
        String vcoreCosts = UnitUtil.transferVcoreS(duration.getSeconds() * ((long) item.getTmCore() * item.getTmNum() + 1));
        String memCost = UnitUtil.transferMemGbS(duration.getSeconds() * ((long) (float) item.getTmMemory() / 1024 * item.getTmNum() + 1));

        info.setTimeCost(timeCost);
        info.setResourceCost(vcoreCosts);
        info.setMemCost(memCost);
        // advice
        List<FlinkTaskAdvice> flinkTaskAdvices = item.getAdvices();
        List<String> advices = new ArrayList<>();
        for (FlinkTaskAdvice flinkTaskAdvice : flinkTaskAdvices) {
            if (flinkTaskAdvice.getHasAdvice() == 1) { // has advice == 1
                advices.add(flinkTaskAdvice.getDescription());
            }
        }

        if (!item.getDiagnosisParallel().equals(item.getParallel())) {
            advices.add(String.format("作业并行度:%d->%d", item.getParallel(), item.getDiagnosisParallel()));
        }
        if (!item.getDiagnosisTmSlotNum().equals(item.getTmSlot())) {
            advices.add(String.format("作业TM的Slot数:%d->%d", item.getTmSlot(), item.getDiagnosisTmSlotNum()));
        }
        if (!item.getDiagnosisTmCoreNum().equals(item.getTmCore())) {
            advices.add(String.format("作业TM的Core数:%d->%d", item.getTmCore(), item.getDiagnosisTmCoreNum()));
        }
        if (!item.getDiagnosisTmMemory().equals(item.getTmMemory())) {
            advices.add(String.format("作业TM的内存:%dMB->%dMB", item.getTmMemory(), item.getDiagnosisTmMemory()));
        }
        if (!item.getDiagnosisJmMemory().equals(item.getJmMemory())) {
            advices.add(String.format("作业JM的内存:%dMB->%dMB", item.getJmMemory(), item.getDiagnosisJmMemory()));
        }
        info.setResourceAdvice(String.join(";", advices));
        return info;
    }
}
