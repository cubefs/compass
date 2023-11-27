package com.oppo.cloud.gpt.drain;


import lombok.Data;

/**
 * Clustering Log
 */
@Data
public class LogCluster {
    /**
     * cluster id
     */
    private String id;
    /**
     * template tokens
     */
    private String[] tokens;
    /**
     * state: NONE, CREATED, UPDATED. it's used to aggregate log.
     */
    private LogClusterState state;

    public LogCluster() {
    }

    public LogCluster(String id, String[] tokens) {
        this.id = id;
        this.tokens = tokens;
    }

    public String getTemplate() {
        return String.join(" ", tokens);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID=").append(id);
        sb.append(" :").append(state);
        sb.append(" : ").append(getTemplate());
        return sb.toString();
    }
}
