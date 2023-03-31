package com.oppo.cloud.common.domain.cluster.hadoop;

import lombok.Data;

import java.util.List;

@Data
public class HadoopConf {

    private List<NameNodeConf> namenodes;

    private List<YarnConf> yarn;

    private SparkConf spark;
}
