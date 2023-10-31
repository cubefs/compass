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
