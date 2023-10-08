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

package com.oppo.cloud.common.domain.cluster.yarn;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * yarn app
 */
@Data
public class YarnApp {

    /**
     * Cluster name
     */
    private String clusterName;
    /**
     * RM IP address
     */
    private String ip;
    /**
     * The application id
     */
    private String id;
    /**
     * The user who started the application
     */
    private String user;
    /**
     * The application name
     */
    private String name;
    /**
     * The queue the application was submitted to
     */
    private String queue;
    /**
     * The application state according to the ResourceManager - valid values are members of the YarnApplicationState
     * enum: NEW, NEW_SAVING, SUBMITTED, ACCEPTED, RUNNING, FINISHED, FAILED, KILLED
     */
    private String state;
    /**
     * The final status of the application if finished - reported by the application itself - valid values are the
     * members of the FinalApplicationStatus enum: UNDEFINED, SUCCEEDED, FAILED, KILLED
     */
    private String finalStatus;
    /**
     * The progress of the application as a percent
     */
    private float progress;
    /**
     * Where the tracking url is currently pointing - History (for history server) or ApplicationMaster
     */
    private String trackingUI;
    /**
     * The web URL that can be used to track the application
     */
    private String trackingUrl;
    /**
     * Detailed diagnostics information
     */
    private String diagnostics;
    /**
     * The cluster id
     */
    private long clusterId;
    /**
     * The application type
     */
    private String applicationType;
    /**
     * Comma separated tags of an application
     */
    private String applicationTags;
    /**
     * The time in which application started (in ms since epoch)
     */
    private long startedTime;
    /**
     * The time in which the application finished (in ms since epoch)
     */
    private long finishedTime;
    /**
     * The elapsed time since the application started (in ms)
     */
    private long elapsedTime;
    /**
     * The URL of the application master container logs
     */
    private String amContainerLogs;
    /**
     * The nodes http address of the application master
     */
    private String amHostHttpAddress;
    /**
     * The sum of memory in MB allocated to the application’s running containers
     */
    private int allocatedMB;
    /**
     * The sum of virtual cores allocated to the application’s running containers
     */
    private int allocatedVCores;
    /**
     * The number of containers currently running for the application
     */
    private int runningContainers;
    /**
     * The amount of memory the application has allocated (megabyte-seconds)
     */
    private long memorySeconds;
    /**
     * The amount of CPU resources the application has allocated (virtual core-seconds)
     */
    private long vcoreSeconds;
    /**
     * Memory used by preempted container
     */
    private long preemptedResourceMB;
    /**
     * Number of virtual cores used by preempted container
     */
    private long preemptedResourceVCores;
    /**
     * Number of standard containers preempted
     */
    private int numNonAMContainerPreempted;
    /**
     * Number of application master containers preempted
     */
    private int numAMContainerPreempted;
    /**
     * Save time
     */
    private long createTime;

    public Map<String, Object> getYarnAppMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("clusterName", this.clusterName);
        m.put("ip", this.ip);
        m.put("id", this.id);
        m.put("user", this.user);
        m.put("name", this.name);
        m.put("queue", this.queue);
        m.put("state", this.state);
        m.put("finalStatus", this.finalStatus);
        m.put("trackingUI", this.trackingUI);
        m.put("trackingUrl", this.trackingUrl);
        m.put("diagnostics", this.diagnostics);
        m.put("clusterId", this.clusterId);
        m.put("applicationType", this.applicationType);
        m.put("applicationTags", this.applicationTags);
        m.put("startedTime", this.startedTime);
        m.put("finishedTime", this.finishedTime);
        m.put("elapsedTime", this.elapsedTime);
        m.put("amContainerLogs", this.amContainerLogs);
        m.put("amHostHttpAddress", this.amHostHttpAddress);
        m.put("allocatedMB", this.allocatedMB);
        m.put("allocatedVCores", this.allocatedVCores);
        m.put("runningContainers", this.runningContainers);
        m.put("memorySeconds", this.memorySeconds);
        m.put("vcoreSeconds", this.vcoreSeconds);
        m.put("preemptedResourceMB", this.preemptedResourceMB);
        m.put("preemptedResourceVCores", this.preemptedResourceVCores);
        m.put("numNonAMContainerPreempted", this.numNonAMContainerPreempted);
        m.put("numAMContainerPreempted", this.numAMContainerPreempted);
        m.put("createTime", this.createTime);
        return m;
    }
}
