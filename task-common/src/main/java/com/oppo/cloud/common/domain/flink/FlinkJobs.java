package com.oppo.cloud.common.domain.flink;

import lombok.Data;

import java.util.List;

@Data
public class FlinkJobs {
    List<FlinkOverviewJob> jobs;
    @Data
    public static class FlinkOverviewJob{
        String id;
        String status;
    }
}
