/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oppo.cloud.common.domain.opensearch;

import com.oppo.cloud.common.util.DateUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class FlinkTaskAnalysis extends OpenSearchInfo {
    /* Flink task app Id */
    private Integer flinkTaskAppId;

    /* Owner of the task: [{userId: 23432, username: "someone"}] */
    private List<SimpleUser> users;

    /* Project name */
    private String projectName;

    /* Project ID */
    private Integer projectId;

    /* Flow name */
    private String flowName;

    /* Flow id */
    private Integer flowId;

    /* task Name */
    private String taskName;

    /* task ID */
    private Integer taskId;

    /* yarn applicationId */
    private String applicationId;

    /* flink track url */
    private String flinkTrackUrl;

    /* Total allocated memory */
    private Integer allocatedMB;

    /* Total allocated vcores */
    private Integer allocatedVcores;

    /* Number of allocated containers */
    private Integer runningContainers;

    /* Executing Engine ? */
    private String engineType;

    /* Execution Date */
    private Date executionDate;

    /* Elapsed time */
    private Double duration;

    /* Start time */
    private Date startTime;

    /* End time */
    private Date endTime;

    /* CPU consumption(vcore-seconds) */
    private Float vcoreSeconds;

    /* Memory consumption(GB-seconds) */
    private Float memorySeconds;

    /* Queue */
    private String queue;

    /* Cluster name */
    private String clusterName;

    /* Times of retries */
    private Integer retryTimes;

    /* Executing user */
    private String executeUser;

    /* Yarn diagnosis information */
    private String diagnosis;

    /* Parallel */
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

    /* Start time of diagnosis */
    private Date diagnosisStartTime;

    /* End time of diagnosis */
    private Date diagnosisEndTime;

    /* Resource diagnosis type:
      - 0: Expand CPU
      - 1: Expand Memory,
      - 2: Reduce CPU,
      - 3: Reduce Memory,
      - 4: Running abnormally */
    private List<Integer> diagnosisResourceType;

    /* Diagnosis source:
      - 0: Midnight scheduled task
      - 1: Diagnosis after task going online
      - 2: Real-time diagnosis */
    private Integer diagnosisSource;

    /* Advice parallel after diagnosis */
    private Integer diagnosisParallel;

    /* Advice JobManager memory(Unit: MB) after diagnosis */
    private Integer diagnosisJmMemory;

    /* Advice TaskManager memory(Unit: MB) after diagnosis */
    private Integer diagnosisTmMemory;

    /* Advice Task slots after diagnosis */
    private Integer diagnosisTmSlotNum;

    /* Advice TaskManager core number after diagnosis */
    private Integer diagnosisTmCoreNum;

    /* Advice TaskManager number after diagnosis */
    private Integer diagnosisTmNum;

    /* Diagnosis Type: [Low memory usage][High CPU peak utilization rate] */
    private List<String> diagnosisTypes;

    /* Processing State: (processing, success, failed) */
    private List<String> processState;

    /* Diagnosis advice */
    private List<FlinkTaskAdvice> advices;

    /* Optimizable number of cores */
    private Long cutCoreNum;

    /* Total number of cores. */
    private Long totalCoreNum;

    /* Optimizable amount of memory */
    private Long cutMemNum;

    /* Total memory */
    private Long totalMemNum;

    /* Create Time */
    private Date createTime;

    /* Update Time */
    private Date updateTime;

    /**
     * Generate doc for storing
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
