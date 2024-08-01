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

import com.oppo.cloud.common.domain.cluster.hadoop.NameNodeConf;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HDFSUtilTest extends ResourcePreparer {

    @Test
    void readLines() throws Exception {
        String logDir = getTextLogDir();
        RemoteIterator<LocatedFileStatus> files = getFileSystem().listFiles(new Path(logDir), true);
        Assertions.assertTrue(files.hasNext());
        while (files.hasNext()) {
            String path = files.next().getPath().toString();
            NameNodeConf nameNode = HDFSUtil.getNameNode(getNameNodeConfMap(), path);
            ReaderObject readerObject = HDFSUtil.getReaderObject(nameNode, path);
            while (true) {
                String line = readerObject.getBufferedReader().readLine();
                if (line == null) {
                    break;
                }
                Assertions.assertTrue(!line.isEmpty());
            }
        }
    }
}
