package com.oppo.cloud.common.domain.flink;

import lombok.Data;

import java.util.List;

/**
 * flink作业
 */
@Data
public class FlinkJobs {
    /**
     * 作业
     */
    List<FlinkOverviewJob> jobs;
    @Data
    public static class FlinkOverviewJob{
        /**
         * id
         */
        String id;
        /**
         * 状态
         */
        String status;
    }
}
