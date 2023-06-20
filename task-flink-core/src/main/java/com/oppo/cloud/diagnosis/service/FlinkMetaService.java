package com.oppo.cloud.diagnosis.service;

import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.flink.JobManagerConfigItem;
import com.oppo.cloud.model.FlinkTaskApp;
import com.oppo.cloud.model.TaskApplication;

import java.util.List;

public interface FlinkMetaService {
    /**
     * 存储实时作业元数据
     */
    void saveRealtimeMetaOnYarn(TaskApplication taskApplication);
    YarnApp requestYarnApp(String appId);
    void fillFlinkMetaWithFlinkConfigOnYarn(FlinkTaskApp flinkTaskApp, List<JobManagerConfigItem> configItems, String jobId);
    List<JobManagerConfigItem> reqFlinkConfig(String trackingUrl);
    String getJobId(String trackingUrl);
    List<String> getTmIds(String trackingUrl);
}
