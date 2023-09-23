package com.oppo.cloud.parser.utils;

import com.oppo.cloud.common.constant.ProtocolType;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.URL;

@Slf4j
public class ResourcePreparer extends MiniHdfsCluster {
    private static String LOCAL_TEXT_LOG_DIR = "/log/text/";
    private static String HDFS_TEXT_LOG_DIR = "/logs";
    @BeforeAll
    static void prepareResources() throws IOException {
        final URL resourcesDir = ResourcePreparer.class.getResource(LOCAL_TEXT_LOG_DIR);
        final FileSystem fs = getFileSystem();
        if (fs != null) {
            fs.mkdirs(new Path(HDFS_TEXT_LOG_DIR));
            fs.copyFromLocalFile(new Path(resourcesDir.getPath()), new Path(HDFS_TEXT_LOG_DIR));
        } else {
            log.error("Got filesystem is null, maybe miniDFSCluster is not ready.");
            throw new IOException("Get FileSystem failed.");
        }
    }

    public String getTextLogDir() {
        return getNameNodeAddress() + HDFS_TEXT_LOG_DIR;
    }
}
