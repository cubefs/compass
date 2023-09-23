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

import com.oppo.cloud.common.constant.Constant;
import com.oppo.cloud.common.domain.cluster.hadoop.NameNodeConf;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedExceptionAction;
import java.util.*;

/**
 * HDFSUtil
 */
@Slf4j
public class HDFSUtil {

    /**
     * get hdfs NameNode
     */
    public static NameNodeConf getNameNode(Map<String, NameNodeConf> nameNodeMap, String path) {
        for (String key : nameNodeMap.keySet()) {
            if (key != null && !key.isEmpty() && path.contains(key)) {
                return nameNodeMap.get(key);
            }
        }
        return null;
    }

     private static FileSystem getFileSystem(NameNodeConf nameNodeConf) throws Exception {
        Configuration conf = new Configuration(false);
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);

        if (nameNodeConf.getNamenodes().length == 1) {
            String defaultFs =
                    String.format("%s%s:%s", Constant.HDFS_SCHEME, nameNodeConf.getNamenodesAddr()[0], nameNodeConf.getPort());
            conf.set("fs.defaultFS", defaultFs);
            if (nameNodeConf.isEnableKerberos()) {
                return getAuthenticationFileSystem(nameNodeConf, conf);
            }
            URI uri = new URI(defaultFs);
            return FileSystem.get(uri, conf);
        }

        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");

        String nameservices = nameNodeConf.getNameservices();

        conf.set("fs.defaultFS", Constant.HDFS_SCHEME + nameservices);
        conf.set("dfs.nameservices", nameservices);
        conf.set("dfs.client.failover.proxy.provider." + nameservices,
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");

        for (int i = 0; i < nameNodeConf.getNamenodes().length; i++) {
            String r = nameNodeConf.getNamenodes()[i];
            conf.set("dfs.namenode.rpc-address." + nameNodeConf.getNameservices() + "." + r,
                    nameNodeConf.getNamenodesAddr()[i] + ":" + nameNodeConf.getPort());
        }

        String nameNodes = String.join(",", nameNodeConf.getNamenodes());
        conf.set("dfs.ha.namenodes." + nameNodeConf.getNameservices(), nameNodes);
        URI uri = new URI(Constant.HDFS_SCHEME + nameservices);
        if (StringUtils.isNotBlank(nameNodeConf.getUser())) {
            System.setProperty("HADOOP_USER_NAME", nameNodeConf.getUser());
        }
        if (StringUtils.isNotBlank(nameNodeConf.getPassword())) {
            System.setProperty("HADOOP_USER_PASSWORD", nameNodeConf.getPassword());
        }
        if (nameNodeConf.isEnableKerberos()) {
            return getAuthenticationFileSystem(nameNodeConf, conf);
        }
        return FileSystem.get(uri, conf);
    }

    private static FileSystem getAuthenticationFileSystem(NameNodeConf nameNodeConf, Configuration conf) throws Exception {
        conf.set("hadoop.security.authorization", "true");
        conf.set("hadoop.security.authentication", "kerberos");
        System.setProperty("java.security.krb5.conf", nameNodeConf.getKrb5Conf());
        conf.set("dfs.namenode.kerberos.principal.pattern", nameNodeConf.getPrincipalPattern());
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab(nameNodeConf.getLoginUser(), nameNodeConf.getKeytabPath());
        UserGroupInformation ugi = UserGroupInformation.getLoginUser();
        return ugi.doAs((PrivilegedExceptionAction<FileSystem>) () -> FileSystem.get(conf));
    }

    public static String[] readLines(NameNodeConf nameNode, String logPath) throws Exception {
        FSDataInputStream fsDataInputStream = null;
        try {
            FileSystem fs = HDFSUtil.getFileSystem(nameNode);
            fsDataInputStream = fs.open(new Path(logPath));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 64kb
            byte[] buffer = new byte[65536];
            int byteRead;

            while ((byteRead = fsDataInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
            byte[] contents = outputStream.toByteArray();
            String s = new String(contents, StandardCharsets.UTF_8);
            return s.split("\n");
        } catch (Exception e) {
            throw new Exception();
        } finally {
            if (Objects.nonNull(fsDataInputStream)) {
                fsDataInputStream.close();
            }
        }
    }

    public static ReaderObject getReaderObject(NameNodeConf nameNode, String path) throws Exception {
        FileSystem fs = HDFSUtil.getFileSystem(nameNode);
        FSDataInputStream fsDataInputStream = fs.open(new Path(path));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream));
        ReaderObject readerObject = new ReaderObject();
        readerObject.setLogPath(path);
        readerObject.setBufferedReader(bufferedReader);
        readerObject.setFsDataInputStream(fsDataInputStream);
        readerObject.setFs(fs);
        return readerObject;
    }


    public static List<String> listFiles(NameNodeConf nameNode, String path) throws Exception {
        FileSystem fs = HDFSUtil.getFileSystem(nameNode);
        RemoteIterator<LocatedFileStatus> it = fs.listFiles(new Path(path), true);
        List<String> result = new ArrayList<>();
        while (it.hasNext()) {
            LocatedFileStatus locatedFileStatus = it.next();
            result.add(locatedFileStatus.getPath().toString());
        }
        fs.close();
        return result;
    }


    public static List<String> filesPattern(NameNodeConf nameNodeConf, String filePath) throws Exception {
        FileSystem fs = HDFSUtil.getFileSystem(nameNodeConf);
        FileStatus[] fileStatuses = fs.globStatus(new Path(filePath));
        List<String> result = new ArrayList<>();
        if (fileStatuses == null) {
            return result;
        }
        for (FileStatus fileStatus : fileStatuses) {
            if (fs.exists(fileStatus.getPath())) {
                result.add(fileStatus.getPath().toString());
            }
        }
        fs.close();
        return result;
    }

}
