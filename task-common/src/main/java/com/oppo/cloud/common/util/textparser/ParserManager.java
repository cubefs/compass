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

package com.oppo.cloud.common.util.textparser;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 日志解析管理
 */
@Slf4j
public class ParserManager {

    /**
     * 默认单线程处理
     */
    public static Map<String, ParserAction> parse(String[] lines, List<ParserAction> parserActions) {
        if (lines == null || parserActions == null) {
            return null;
        }
        // 获取解析到根结点actions
        Map<String, ParserAction> rootActions = parseRootAction(lines, parserActions);

        for (Map.Entry<String, ParserAction> action : rootActions.entrySet()) {
            parseChildActions(action.getValue());
        }

        return rootActions;
    }

    /**
     * 多线程处理，传入线程池
     */
    public static Map<String, ParserAction> parse(String[] lines, List<ParserAction> parserActions, Executor executor) {
        if (executor == null) {
            return parse(lines, parserActions);
        }
        // 获取解析到的根结点
        Map<String, ParserAction> rootActions = parseRootAction(lines, parserActions);

        CompletableFuture[] array = new CompletableFuture[rootActions.size()];

        int i = 0;
        for (Map.Entry<String, ParserAction> action : rootActions.entrySet()) {
            array[i] = CompletableFuture.supplyAsync(() -> {
                parseChildActions(action.getValue());
                return null;
            }, executor);
            i++;
        }
        try {
            CompletableFuture.allOf(array).get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return rootActions;
    }

    /**
     * 解析根结点action
     */
    public static Map<String, ParserAction> parseRootAction(String[] lines, List<ParserAction> parserActions) {
        TextParser headTextParser = new TextParser(parserActions);
        for (String line : lines) {
            headTextParser.parse(line);
        }
        return headTextParser.getResults();
    }

    /**
     * 解析子节点action
     */
    public static void parseChildActions(ParserAction root) {
        Deque<ParserAction> nodeDeque = new LinkedList<>();
        ParserAction node = root;
        int index = 0;
        nodeDeque.push(node);
        while (!nodeDeque.isEmpty()) {
            node = nodeDeque.pop();
            if (index != 0) {
                parseChildNode(node);
            }
            index++;
            List<ParserAction> children = node.getChildren();
            if (children != null) {
                for (int i = children.size() - 1; i >= 0; i--) {
                    ParserAction child = children.get(i);
                    child.setRootResults(node.getParserResults());
                    nodeDeque.push(children.get(i));
                }
            }
        }
    }

    /**
     * 子节点解析
     */
    private static void parseChildNode(ParserAction node) {
        TextParser nextParser = new TextParser(node);
        List<ParserResult> rootResults = node.getRootResults();
        // 针对block合并行
        if (node.getParserType().equals(ParserType.JOIN)) {
            rootResults = setJoinResults(rootResults);
        }
        for (ParserResult parserResult : rootResults) {
            for (String line : parserResult.getLines()) {
                nextParser.parse(line);
            }
        }
    }

    /**
     * 块文本转字符串
     */
    private static List<ParserResult> setJoinResults(List<ParserResult> parserResults) {
        List<ParserResult> results = new ArrayList<>();
        for (ParserResult parserResult : parserResults) {
            String joinString = String.join("", parserResult.getLines());
            ParserResult result = new ParserResult();
            List<String> lines = new ArrayList<>();
            lines.add(joinString);
            result.setLines(lines);
            result.setGroupData(parserResult.getGroupData());
            results.add(result);
        }
        return results;
    }
}
