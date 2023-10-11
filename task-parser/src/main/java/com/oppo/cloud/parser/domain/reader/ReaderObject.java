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

package com.oppo.cloud.parser.domain.reader;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;

import java.io.BufferedReader;
import java.io.IOException;

@Data
@Slf4j
public class ReaderObject {

    private String logPath;

    private BufferedReader bufferedReader;

    private FSDataInputStream fsDataInputStream;

    private FileSystem fs;

    public void close() {
        try {
            if (fsDataInputStream != null) {
                fsDataInputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fs != null) {
                // TODO: should close fs ? the fs may be a share object if we use FileSystem.get internal cache mechanisam
                fs.close();
            }
        } catch (IOException e) {
            log.error("close file: {}, exception: ", logPath, e);
        }
    }
}
