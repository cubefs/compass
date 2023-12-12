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

package com.oppo.cloud.parser.service.job.parser;

import com.oppo.cloud.common.constant.LogType;
import com.oppo.cloud.common.constant.ProgressState;
import com.oppo.cloud.common.domain.job.LogPath;
import com.oppo.cloud.common.domain.oneclick.OneClickProgress;
import com.oppo.cloud.common.domain.oneclick.ProgressInfo;
import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.common.util.textparser.ParserManager;
import com.oppo.cloud.common.util.textparser.TextParser;
import com.oppo.cloud.parser.config.DiagnosisConfig;
import com.oppo.cloud.parser.domain.job.CommonResult;
import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.domain.job.SparkExecutorLogParserResult;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.service.reader.IReader;
import com.oppo.cloud.parser.service.reader.LogReaderFactory;
import com.oppo.cloud.parser.service.writer.OpenSearchWriter;
import com.oppo.cloud.parser.utils.GCReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Slf4j
public class SparkExecutorLogParser extends CommonTextParser {

    private final ParserParam param;

    private final boolean isOneClick;

    private final Executor parserThreadPool;

    private final List<String> jvmTypeList;

    public SparkExecutorLogParser(ParserParam param,
                                  ThreadPoolTaskExecutor threadPool,
                                  List<String> jvmTypeList) {
        this.param = param;
        this.isOneClick = param.getLogRecord().getIsOneClick();
        this.parserThreadPool = threadPool;
        this.jvmTypeList = jvmTypeList;
    }

    @Override
    public CommonResult run() {
        updateParserProgress(ProgressState.PROCESSING, 0, 0);
        CommonResult<List<SparkExecutorLogParserResult>> commonResult = new CommonResult<>();
        List<SparkExecutorLogParserResult> parserResults = new ArrayList<>();
        for (LogPath logPath : this.param.getLogPaths()) {
            List<ReaderObject> readerObjects;
            try {
                IReader reader = LogReaderFactory.create(logPath);
                readerObjects = reader.getReaderObjects();
            } catch (Exception e) {
                log.error("Exception:", e);
                continue;
            }
            if (readerObjects.size() > 0) {
                updateParserProgress(ProgressState.PROCESSING, 0, readerObjects.size());
                parserResults.addAll(handleReaderObjects(readerObjects));
            }
        }
        updateParserProgress(ProgressState.SUCCEED, 0, 0);
        commonResult.setLogType(this.param.getLogType());
        commonResult.setResult(parserResults);
        return commonResult;

    }

    private List<SparkExecutorLogParserResult> handleReaderObjects(List<ReaderObject> readerObjects) {
        List<CompletableFuture<SparkExecutorLogParserResult>> futures = new ArrayList<>();
        for (ReaderObject readerObject : readerObjects) {
            CompletableFuture<SparkExecutorLogParserResult> future =
                    CompletableFuture.supplyAsync(() -> handleReaderObject(readerObject), parserThreadPool);
            futures.add(future);
        }
        List<SparkExecutorLogParserResult> results = new ArrayList<>();
        int i = 0;
        for (Future<SparkExecutorLogParserResult> result : futures) {
            SparkExecutorLogParserResult sp = null;
            try {
                sp = result.get();
            } catch (Exception e) {
                log.error("Exception:", e);
            }
            updateParserProgress(ProgressState.PROCESSING, i++, readerObjects.size());
            if (sp != null) {
                results.add(sp);
            }
        }
        return results;
    }

    private SparkExecutorLogParserResult handleReaderObject(ReaderObject readerObject) {
        String logType = getLogType(readerObject.getLogPath());
        SparkExecutorLogParserResult result = null;
        try {
            result = parseAction(logType, readerObject);
        } catch (Exception e) {
            log.error("Exception:", e);
        } finally {
            readerObject.close();
        }
        if (result != null && result.getActionMap() != null) {
            List<String> categories = OpenSearchWriter.getInstance()
                    .saveParserActions(logType, readerObject.getLogPath(), this.param, result.getActionMap());
            result.setCategories(categories);
        }
        return result;
    }


    private SparkExecutorLogParserResult parseAction(String logType, ReaderObject readerObject) throws Exception {
        SparkExecutorLogParserResult result = parseRootAction(logType, readerObject);
        for (Map.Entry<String, ParserAction> action : result.getActionMap().entrySet()) {
            ParserManager.parseChildActions(action.getValue());
        }
        return result;
    }

    private SparkExecutorLogParserResult parseRootAction(String logType, ReaderObject readerObject) throws Exception {
        List<ParserAction> actions = DiagnosisConfig.getInstance().getActions(logType);
        Map<Integer, byte[]> gcLogMap = new HashMap<>();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean isGCLog = false;
        boolean isStderr = false;

        TextParser headTextParser = new TextParser(actions);
        BufferedReader bufferedReader = readerObject.getBufferedReader();
        while (true) {
            String line;
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                log.error(e.getMessage());
                break;
            }
            if (line == null) {
                break;
            }
            headTextParser.parse(line);

            // get gc log
            if (line.contains("stderr")) {
                isGCLog = false;
                if (LogType.SPARK_DRIVER.getName().equals(logType) && byteArrayOutputStream.size() > 0) {
                    gcLogMap.put(0, byteArrayOutputStream.toByteArray());
                }
                isStderr = true;
            }

            if (jvmTypeList != null && !isGCLog) {
                for (String jvm : jvmTypeList) {
                    if (line.contains(jvm)) {
                        isGCLog = true;
                        line = jvm + line.split(jvm)[1];
                        break;
                    }
                }
            }

            if (isGCLog) {
                line += "\n";
                byteArrayOutputStream.write(line.getBytes());
            }
            if (isStderr && line.contains("Starting executor ID")) {
                String id = line.split("ID")[1].split("on")[0].trim();
                if (byteArrayOutputStream.size() > 0) {
                    String gcLog = byteArrayOutputStream.toString();
                    log.debug("gcLog:{}\n{}", readerObject.getLogPath(), gcLog);
                    gcLogMap.put(Integer.valueOf(id), byteArrayOutputStream.toByteArray());
                    byteArrayOutputStream = new ByteArrayOutputStream();
                }
            }

        }

        SparkExecutorLogParserResult result = new SparkExecutorLogParserResult();
        result.setActionMap(headTextParser.getResults());
        if (gcLogMap.size() > 0) {
            result.setGcReports(GCReportUtil.generateGCReports(gcLogMap, readerObject.getLogPath()));
        }
        result.setLogPath(readerObject.getLogPath());

        return result;
    }


    private String getLogType(String logPath) {
        if (logPath.contains(this.param.getApp().getAmHost())) {
            return LogType.SPARK_DRIVER.getName();
        }
        return LogType.SPARK_EXECUTOR.getName();
    }


    public void updateParserProgress(ProgressState state, Integer progress, Integer count) {
        if (!this.isOneClick) {
            return;
        }
        OneClickProgress oneClickProgress = new OneClickProgress();
        oneClickProgress.setAppId(this.param.getApp().getAppId());
        oneClickProgress.setLogType(LogType.SPARK_EXECUTOR);
        ProgressInfo executorProgress = new ProgressInfo();
        executorProgress.setCount(count);
        executorProgress.setProgress(progress);
        executorProgress.setState(state);
        oneClickProgress.setProgressInfo(executorProgress);
        super.update(oneClickProgress);
    }
}
