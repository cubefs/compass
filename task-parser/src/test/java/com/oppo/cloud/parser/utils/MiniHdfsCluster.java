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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.util.ShutdownHookManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

@Slf4j
public class MiniHdfsCluster extends ParserConfigLoader {
    private static MiniDFSCluster hdfsCluster;
    private static Integer nameNodePort = 8020;

    @BeforeAll
    static void startMiniDFS() throws IOException {
        if (hdfsCluster == null) {
            // TODO read properties from yml file like HDFSUtil#getFileSystem
            // TODO support HA DFS
            try {
                Configuration conf = new Configuration();
                hdfsCluster = new MiniDFSCluster.Builder(conf)
                        .nameNodePort(nameNodePort)
                        .build();
                hdfsCluster.waitClusterUp();
                registerShutdownHook();
            } catch (IOException e) {
                log.warn("Set up miniDFSCluster failed.", e);
                throw e;
            }
        }
    }

    public static String getNameNodeAddress() {
        if (hdfsCluster != null) {
            return "hdfs://" + hdfsCluster.getNameNode().getHostAndPort();
        }
        return null;
    }

    public static FileSystem getFileSystem() throws IOException {
        if (hdfsCluster != null) {
            return hdfsCluster.getFileSystem(0);
        }
        return null;
    }


    static void registerShutdownHook() {
        ShutdownHookManager.get().addShutdownHook((Runnable) () -> {
            if (hdfsCluster != null) {
                hdfsCluster.shutdown();
            }
        }, 1);
    }
}
