package com.oppo.cloud.common.domain.opensearch;

import com.oppo.cloud.common.util.DateUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class FlinkTaskAnalysis extends OpenSearchInfo {
    /* Flink task app Id */
    private Integer flinkTaskAppId;

    /* 任务所属用户: [{userId: 23432, username: "someone"}] */
    private List<SimpleUser> users;

    /* 项目名称 */
    private String projectName;

    /* 项目ID */
    private Integer projectId;

    /* 工作流名称 */
    private String flowName;

    /* 工作流Id */
    private Integer flowId;

    /* 任务名称 */
    private String taskName;

    /* 任务ID */
    private Integer taskId;

    /* yarn applicationId */
    private String applicationId;

    /* flink track url */
    private String flinkTrackUrl;

    /* yarn获取的总共分配mb */
    private Integer allocatedMB;

    /* yarn获取的总共分配vcore */
    private Integer allocatedVcores;

    /* yarn获取的总共分配容器 */
    private Integer runningContainers;

    /* 执行引擎? */
    private String engineType;

    /* 执行周期 */
    private Date executionDate;

    /* 运行耗时 */
    private Double duration;

    /* 开始时间 */
    private Date startTime;

    /* 结束时间 */
    private Date endTime;

    /* cpu消耗(vcore-seconds) */
    private Float vcoreSeconds;

    /* 内存消耗(GB-seconds) */
    private Float memorySeconds;

    /* 队列名称 */
    private String queue;

    /* 集群名称 */
    private String clusterName;

    /* 重试次数 */
    private Integer retryTimes;

    /* 执行用户 */
    private String executeUser;

    /* yarn诊断信息 */
    private String diagnosis;

    /* 并行度 */
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

    /* flink job name */
    private String jobName;

    /* 诊断开始时间 */
    private Date diagnosisStartTime;

    /* 诊断结束时间 */
    private Date diagnosisEndTime;

    /* 资源诊断类型,[0扩容cpu,1扩容mem,2缩减cpu,3缩减mem,4运行异常] */
    private List<Integer> diagnosisResourceType;

    /* 诊断来源0凌晨定时任务,1任务上线后诊断,2即时诊断 */
    private Integer diagnosisSource;

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

    /* 建议tm数量 */
    private Integer diagnosisTmNum;

    /* 诊断类型: [内存使用率低][CPU峰值利用率高] */
    private List<String> diagnosisTypes;

    /* 处理状态(processing, success, failed) */
    private List<String> processState;

    /* 诊断建议 */
    private List<FlinkTaskAdvice> advices;

    /* 可优化核数 */
    private Long cutCoreNum;

    /* 总核数 */
    private Long totalCoreNum;

    /* 可优化内存数 */
    private Long cutMemNum;

    /* 总内存 */
    private Long totalMemNum;

    /* 记录创建时间 */
    private Date createTime;

    /* 记录更新时间 */
    private Date updateTime;

    /**
     * 生成文档记录
     *
     * @return
     * @throws Exception
     */
    public Map<String, Object> genDoc() throws Exception {
        Map<String, Object> doc = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            String key = field.getName();
            String method = key.substring(0, 1).toUpperCase() + key.substring(1);
            Method getMethod = this.getClass().getMethod("get" + method);
            switch (field.getName()) {
                case "docId":
                    break;
                case "diagnosisStartTime":
                case "diagnosisEndTime":
                case "startTime":
                case "endTime":
                case "executionDate":
                case "updateTime":
                case "createTime":
                    Date value = (Date) getMethod.invoke(this);
                    if (value != null) {
                        doc.put(key, DateUtil.timestampToUTCDate(value.getTime()));
                    }
                    break;
                default:
                    doc.put(key, getMethod.invoke(this));
            }
        }
        return doc;
    }

    public String genIndex(String baseIndex) {
        return StringUtils.isNotBlank(this.getIndex()) ? this.getIndex() :
                baseIndex + "-" + DateUtil.format(this.createTime, "yyyy-MM-dd");
    }

    public String genDocId() {
        return StringUtils.isNotBlank(this.getDocId()) ? this.getDocId() : UUID.randomUUID().toString();
    }
}
