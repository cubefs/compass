package com.oppo.cloud.portal.domain.realtime;

/**
 * redis缓存key常量
 */
public class Constant {
    /**
     * 小时毫秒数值
     */
    public static final long HOUR_MS = 60 * 60 * 1000;
    /**
     * 天秒数值
     */
    public static final long DAY_SECONDS = 24 * 60 * 60;
    /**
     * spark event log路径
     */
    public static final String SPARK_EVENT_LOG_DIRECTORY = "spark_event_log_directory:";
    /**
     * spark历史服务器缓存key
     */
    public static final String SPARK_HISTORY_SERVERS = "spark_history_servers:";
    /**
     * spark app缓存key前缀
     */
    public static final String SPARK_APP = "spark_app:";

    /**
     * yarn app缓存key前缀
     */
    public static final String YARN_APP = "yarn_app:";
    /**
     * yarn集群缓存key
     */
    public static final String YARN_CLUSTERS = "yarn_clusters:";
    /**
     * resourceManager对应jobHistoryServer
     */
    public static final String RM_JHS_MAP = "rm_jhs_map:";
    /**
     * jobHistoryServer hdfs路径
     */
    public static final String JHS_HDFS_PATH = "jhs_hdfs_path:";
    /**
     * spark app es前缀
     */
    public static final String SPARK_APP_ES_INDEX_PREFIX = "spark-app-tob-";
    /**
     * YARN app es前缀
     */
    public static final String YARN_APP_ES_INDEX_PREFIX = "yarn-app-tob-";

    public static final String REALTIME_DIAGNOSIS_REPORT_ES_INDEX_PREFIX = "realtime-report-";
}
