package com.oppo.cloud.common.domain.flink;

import lombok.Data;

import java.util.List;

/**
 * flink tm
 */
@Data
public class FlinkTms {
    /**
     * tms
     */
    List<FlinkTmsTaskManager> taskmanagers;
    @Data
    public static class FlinkTmsTaskManager{
        /**
         * id
         */
        String id;
        /**
         * path
         */
        String path;
    }
}
