package com.oppo.cloud.meta.domain;

import lombok.Data;

@Data
public class YarnPathInfo {
    /**
     * fs.defaultFS
     */
    private String defaultFS;

    /**
     * yarn.nodemanager.remote-app-log-dir
     */
    private String remoteDir;

    /**
     * mapreduce.jobhistory.done-dir
     */
    private String mapreduceDoneDir;

    /**
     * mapreduce.jobhistory.intermediate-done-dir
     */
    private String mapreduceIntermediateDoneDir;
}
