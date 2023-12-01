package com.oppo.cloud.gpt.drain;

public enum LogClusterState {
    NONE, // exist, does not need to update
    CLUSTER_CREATED, // create new cluster
    CLUSTER_CHANGED, // cluster exist, but have to update
}
