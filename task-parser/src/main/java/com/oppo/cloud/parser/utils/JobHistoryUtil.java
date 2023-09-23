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

import com.oppo.cloud.parser.domain.mr.JobHistoryFileInfo;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JobHistoryUtil {

    private static final String PROPERTY = "property";

    private static final String NAME = "name";

    private static final String VALUE = "value";


    /**
     * Job History File extension.
     */
    public static final String JOB_HISTORY_FILE_EXTENSION = ".jhist";

    /**
     * Suffix for configuration files.
     */
    public static final String JOB_CONF_FILE_NAME_SUFFIX = "_conf.xml";


    public static boolean isJobHistoryFile(String file) {
        return file.endsWith(JOB_HISTORY_FILE_EXTENSION);
    }

    public static boolean isJobConfFile(String file) {
        return file.endsWith(JOB_CONF_FILE_NAME_SUFFIX);
    }

    public static JobHistoryFileInfo getJobHistoryFileInfo(List<ReaderObject> readerObjects) {
        ReaderObject jobHistoryReader = null;
        ReaderObject confReader = null;
        for (ReaderObject readerObject : readerObjects) {
            if (JobHistoryUtil.isJobHistoryFile(readerObject.getLogPath())) {
                jobHistoryReader = readerObject;
            }
            if (JobHistoryUtil.isJobConfFile(readerObject.getLogPath())) {
                confReader = readerObject;
            }
        }
        return new JobHistoryFileInfo(jobHistoryReader, confReader);
    }

    public static Map<String, String> parseJobConf(ReaderObject confReader) {
        Map<String, String> map = new HashMap<>();
        if (confReader == null || confReader.getBufferedReader() == null) {
            return map;
        }
        try {
            String line = confReader.getBufferedReader().lines().collect(Collectors.joining());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(line.getBytes()));
            Element root = document.getDocumentElement();
            NodeList nodeList = root.getElementsByTagName(PROPERTY);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String name = element.getElementsByTagName(NAME).item(0).getTextContent();
                String value = element.getElementsByTagName(VALUE).item(0).getTextContent();
                map.put(name, value);
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
        }
        return map;
    }


    public static MRAppInfo parseJobHistory(List<ReaderObject> readerObjects) throws Exception {
        JobHistoryFileInfo jobHistoryFileInfo = getJobHistoryFileInfo(readerObjects);
        Map<String, String> conMap = parseJobConf(jobHistoryFileInfo.getConfReader());
        FSDataInputStream fsDataInputStream = jobHistoryFileInfo.getJobHistoryReader().getFsDataInputStream();
        ReplayMREventLogs replayMREventLogs = new ReplayMREventLogs(fsDataInputStream);
        replayMREventLogs.parse();
        MRAppInfo appData = replayMREventLogs.getMRAppInfo();
        appData.setConfMap(conMap);
        return appData;
    }


}