package com.oppo.cloud.diagnosis.constant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class DiagnosisParamsConstants {
    // 默认缩减率
    public Double parallelCutRate = 0.1d;
    // 默认扩容率
    public Double tmParallelGrowRate = 0.1d;
    // 单tm cpu利用率太低的阈值
    public Double tmCpuUsageCutThreshold = 0.4d;
    // 单tm cpu利用率太低的目标值
    public Double tmCpuUsageCutTarget = 0.6d;
    // cpu过高的阈值
    public Double tmCpuUsageGrowThreshold = 0.8d;
    // 单tm峰值cpu利用率目标最大值
    public Double tmCpuUsageGrowTarget = 0.6d;
    // 单tm mem利用率太低的阈值
    public Double tmMemUsageLowThreshold = 0.4d;
    // 单tm mem利用率太低的目标值
    public Double tmMemUsageLowTarget = 0.6d;
    // mem 太高阈值
    public Double tmMemUsageHighThreshold = 0.8d;
    // 单tm mem利用率目标值
    public Double tmMemUsageHighTarget = 0.6d;
    // 单tm峰值利用率太高的持续时间阈值秒
    public Float tmPeakHighTimeThreshold = 1800f;

    // cpu 使用率累计高的高的时间
    public Float cpuUsageAccHighTimeRate = 0.2f;
    // 诊断最大从周期内该时间后开始诊断
    public Integer diagnosisAfterMinutesMax = 60;
    // 诊断判断追数据结束的lag最低值
    public Integer diagnosisMinDelayAfterRunning = 100;
    // 扩容时取最近n分钟的cpu均值
    public Integer tmCpuHighLatestNMinutes = 5;
    // 追延迟时判断延迟时间是否超过这个值，超过了则需要追延迟
    public Float catchUpDelayThreshold = 300f;
    // 追延迟时消费速率与生产速率比值最大值阈值
    public Float catchUpConsumeDivideProduceThreshold = 3.0f;
    // 任务伸缩最小间隔分钟
    public Float elasticMinInterval = 5.0f;

    // jm内存在100个tm内时设置为1g
    public Integer jm1gTmNum = 100;

    // 慢算子的in out buffer pool 使用率差值阈值
    public Double slowVerticesInoutDiffHighThreshold = 0.6;
    // 慢算子的in out buffer pool 使用率差值持续时间阈值秒
    public Integer slowVerticesInoutDiffHighDuration = 300;
    // manage内存期望使用率
    public Float tmManageMemUsageCutThreshold = 0.5f;
    // 流量波谷中心距离边缘的比例
    public Float trafficTroughRatio = 0.7f;

    // tm内存最大值
    public Integer tmMemMax = 6144;
    // tm内存最小值
    public Integer tmMemMin = 1024;

    // tm cpu没有使用的阈值
    public Double tmCpuNoUsageThreshold = 0.05;
    // tm mem没有使用的阈值
    public Double tmHeapMemNoUsageThreshold = 0.1;

    // 任务缩减延迟时间阈值秒
    public Float JOB_CUT_LAG_TIME_THRESHOLD = 600f;
    // 任务缩减延迟时间阈值秒
    public Float JOB_DELAY_LITTLE_HIGH = 300f;

    // 默认作业最大并行度
    public Integer maxParallel = 300;

    // 小时诊断开始时间
    public Integer hourlyDiagnosisStartMinutes = 60;
    // 小时级别在任务上线后start 分钟到end分钟内进行诊断
    public Integer hourlyDiagnosisEndMinutes = 120;
}
