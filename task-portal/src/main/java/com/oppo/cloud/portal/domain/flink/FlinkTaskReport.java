package com.oppo.cloud.portal.domain.flink;

import lombok.Data;

import java.util.Date;

/**
 * OpenSearch Flink Report
 */
@Data
public class FlinkTaskReport {
    /**
     * flink task analysis表关联Id
     */
    private String flinkTaskAnalysisId;
    /**
     * 诊断结果图表Json数据
     */
    private String reportJson;
    /**
     * 创建时间
     */
    private Date createTime;
}
