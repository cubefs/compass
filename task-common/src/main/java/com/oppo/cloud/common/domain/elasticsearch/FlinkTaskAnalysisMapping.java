package com.oppo.cloud.common.domain.elasticsearch;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlinkTaskAnalysisMapping extends Mapping {

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("properties", build()),
                new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> advice() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("ruleName", text());
        properties.put("ruleAlias", text());
        properties.put("ruleCode", digit("integer"));
        properties.put("hasAdvice", digit("integer"));
        properties.put("description", text());
        return Stream.of(
                new AbstractMap.SimpleEntry<>("properties", properties)
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                /* Flink task app Id */
                new AbstractMap.SimpleEntry<>("flinkTaskAppId", digit("integer")),
                /* 任务所属用户: [{userId: 23432, username: "someone"}] */
                new AbstractMap.SimpleEntry<>("users", users()),
                /* 项目名称 */
                new AbstractMap.SimpleEntry<>("projectName", text()),
                /* 项目ID */
                new AbstractMap.SimpleEntry<>("projectId", digit("integer")),
                /* 工作流名称 */
                new AbstractMap.SimpleEntry<>("flowName", text()),
                /* 工作流Id */
                new AbstractMap.SimpleEntry<>("flowId", digit("integer")),
                /* 任务名称 */
                new AbstractMap.SimpleEntry<>("taskName", text()),
                /* 任务ID */
                new AbstractMap.SimpleEntry<>("taskId", digit("integer")),
                /* yarn applicationId */
                new AbstractMap.SimpleEntry<>("applicationId", text()),
                /* flink track url */
                new AbstractMap.SimpleEntry<>("flinkTrackUrl", text()),
                /* yarn获取的总共分配mb */
                new AbstractMap.SimpleEntry<>("allocatedMB", digit("long")),
                /* yarn获取的总共分配vcore */
                new AbstractMap.SimpleEntry<>("allocatedVcores", digit("integer")),
                /* yarn获取的总共分配容器 */
                new AbstractMap.SimpleEntry<>("runningContainers", digit("integer")),
                /* 执行引擎? */
                new AbstractMap.SimpleEntry<>("engineType", text()),
                /* 执行周期 */
                new AbstractMap.SimpleEntry<>("executionDate", date()),
                /* 运行耗时 */
                new AbstractMap.SimpleEntry<>("duration", digit("double")),
                /* 开始时间 */
                new AbstractMap.SimpleEntry<>("startTime", date()),
                /* 结束时间 */
                new AbstractMap.SimpleEntry<>("endTime", date()),
                /* cpu消耗(vcore-seconds) */
                new AbstractMap.SimpleEntry<>("vcoreSeconds", digit("float")),
                /* 内存消耗(GB-seconds) */
                new AbstractMap.SimpleEntry<>("memorySeconds", digit("float")),
                /* 队列名称 */
                new AbstractMap.SimpleEntry<>("queue", text()),
                /* 集群名称 */
                new AbstractMap.SimpleEntry<>("clusterName", text()),
                /* 重试次数 */
                new AbstractMap.SimpleEntry<>("retryTimes", digit("integer")),
                /* 执行用户 */
                new AbstractMap.SimpleEntry<>("executeUser", text()),
                /* yarn诊断信息 */
                new AbstractMap.SimpleEntry<>("diagnosis", text()),
                /* 并行度 */
                new AbstractMap.SimpleEntry<>("parallel", digit("integer")),
                /* flink slot */
                new AbstractMap.SimpleEntry<>("tmSlot", digit("integer")),
                /* flink task manager core */
                new AbstractMap.SimpleEntry<>("tmCore", digit("integer")),
                /* flink task manager memory */
                new AbstractMap.SimpleEntry<>("tmMemory", digit("integer")),
                /* flink job manager memory */
                new AbstractMap.SimpleEntry<>("jmMemory", digit("integer")),
                /* flink task manager num */
                new AbstractMap.SimpleEntry<>("tmNum", digit("integer")),
                /* flink job name */
                new AbstractMap.SimpleEntry<>("jobName", text()),
                /*  诊断开始时间 */
                new AbstractMap.SimpleEntry<>("diagnosisStartTime", date()),
                /*  诊断结束时间 */
                new AbstractMap.SimpleEntry<>("diagnosisEndTime", date()),
                /* 资源诊断类型,[0扩容cpu,1扩容mem,2缩减cpu,3缩减mem,4运行异常] */
                new AbstractMap.SimpleEntry<>("diagnosisResourceType", text()),
                /* 诊断来源0凌晨定时任务,1任务上线后诊断,2即时诊断 */
                new AbstractMap.SimpleEntry<>("diagnosisSource", digit("integer")),
                /* 建议并行度 */
                new AbstractMap.SimpleEntry<>("diagnosisParallel", digit("integer")),
                /* 建议job manager 内存大小单位MB */
                new AbstractMap.SimpleEntry<>("diagnosisJmMemory", digit("integer")),
                /* 建议task manager 内存大小单位MB */
                new AbstractMap.SimpleEntry<>("diagnosisTmMemory", digit("integer")),
                /* 建议tm的slot数量 */
                new AbstractMap.SimpleEntry<>("diagnosisTmSlotNum", digit("integer")),
                /* 建议tm的core数量 */
                new AbstractMap.SimpleEntry<>("diagnosisTmCoreNum", digit("integer")),
                /* 建议tm数量 */
                new AbstractMap.SimpleEntry<>("diagnosisTmNum", digit("integer")),
                /* 诊断类型 */
                new AbstractMap.SimpleEntry<>("diagnosisTypes", text()),
                /* 处理状态(processing, success, failed) */
                new AbstractMap.SimpleEntry<>("processState", text()),
                /* 诊断建议：[{ruleName, ruleType, hasAdvice, description}, {...}] */
                new AbstractMap.SimpleEntry<>("advices", advice()),
                /* 可优化核数 */
                new AbstractMap.SimpleEntry<>("cutCoreNum", digit("long")),
                /* 总核数 */
                new AbstractMap.SimpleEntry<>("totalCoreNum", digit("long")),
                /* 可优化内存数 */
                new AbstractMap.SimpleEntry<>("cutMemNum", digit("long")),
                /* 总内存 */
                new AbstractMap.SimpleEntry<>("totalMemNum", digit("long")),
                /* 记录创建时间 */
                new AbstractMap.SimpleEntry<>("createTime", date()),
                /* 记录更新时间 */
                new AbstractMap.SimpleEntry<>("updateTime", date())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
