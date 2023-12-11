/*
 * SPDX-License-Identifier: MIT
 * This file implements the Drain algorithm for log parsing.
 * Based on https://github.com/logpai/logparser/blob/master/logparser/Drain/Drain.py by LogPAI team
 */

package com.oppo.cloud.gpt.drain;

public enum LogClusterState {
    NONE, // exist, does not need to update
    CLUSTER_CREATED, // create new cluster
    CLUSTER_CHANGED, // cluster exist, but have to update
}
