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

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class ResourcePreparer extends MiniHdfsCluster {
    private static String LOCAL_TEXT_LOG_DIR = "/log/";
    private static String HDFS_TEXT_LOG_DIR = "/log";

    static {
        ParserConfigLoader.init();
    }

    @BeforeAll
    public static void prepareResources() throws IOException {
        final URL resourcesDir = ResourcePreparer.class.getResource(LOCAL_TEXT_LOG_DIR);
        final FileSystem fs = getFileSystem();
        if (fs != null) {
            uploadDirectory(resourcesDir.getPath(), HDFS_TEXT_LOG_DIR, fs);
        } else {
            log.error("Got filesystem is null, maybe miniDFSCluster is not ready.");
            throw new IOException("Get FileSystem failed.");
        }
    }

    private static void uploadDirectory(String localDir,
                                        String hdfsDir,
                                        FileSystem fs) throws IOException {
        Path hdfsDirPath = new Path(hdfsDir);
        if (!fs.exists(hdfsDirPath)) {
            fs.mkdirs(hdfsDirPath);
        }
        File localDirFile = new File(localDir);
        for (File file : localDirFile.listFiles()) {
            String targetFilePath = hdfsDir + File.separator + file.getName();
            if (file.isDirectory()) {
                uploadDirectory(file.getPath(), targetFilePath, fs);
            } else {
                fs.copyFromLocalFile(new Path(file.getPath()), new Path(targetFilePath));
            }
        }
    }

    public String getTextLogDir() {
        return getNameNodeAddress() + HDFS_TEXT_LOG_DIR;
    }
}
