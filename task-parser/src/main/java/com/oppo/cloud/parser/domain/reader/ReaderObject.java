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

import com.github.luben.zstd.ZstdInputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4BlockInputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.xerial.snappy.SnappyInputStream;

import java.io.*;
import java.util.Locale;

@Slf4j
public class ReaderObject {

    @Getter
    @Setter
    private String logPath;
    private BufferedReader bufferedReader;
    @Getter
    @Setter
    private FSDataInputStream fsDataInputStream;
    @Getter
    @Setter
    private FileSystem fs;

    public BufferedReader getBufferedReader(String compressCodec) throws IOException {
        if (bufferedReader != null) {
            return bufferedReader;
        }
        InputStream inputStream;
        switch (compressCodec.toLowerCase(Locale.ROOT)) {
            case "lz4":
                inputStream = new LZ4BlockInputStream(fsDataInputStream, false);
                break;
            case "snappy":
                inputStream = new SnappyInputStream(fsDataInputStream);
                break;
            case "zstd":
                inputStream = new BufferedInputStream(new ZstdInputStream(fsDataInputStream), 32 * 1023);
                break;
            default:
                inputStream = fsDataInputStream;
                break;
        }
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        return bufferedReader;
    }

    public BufferedReader getBufferedReader() throws IOException {
        if (bufferedReader != null) {
            return bufferedReader;
        }
        return getBufferedReader("none");
    }

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
