package com.oppo.cloud.common.domain.flink;

import lombok.Data;

import java.util.List;

@Data
public class FlinkTms {
    List<FlinkTmsTaskManager> taskmanagers;
    @Data
    public static class FlinkTmsTaskManager{
        String id;
        String path;
    }
}
