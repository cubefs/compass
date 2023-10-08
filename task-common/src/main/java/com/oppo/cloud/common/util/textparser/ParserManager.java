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
 * Log parsing management
 */
@Slf4j
public class ParserManager {

    /**
     * By default, process in a single thread.
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
     *  Perform multi-threaded processing and pass it to the thread pool.
     */
    public static Map<String, ParserAction> parse(String[] lines, List<ParserAction> parserActions, Executor executor) {
        if (executor == null) {
            return parse(lines, parserActions);
        }
        // Get the parsed actions to the root node.
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
     * Get the parsed root node.
     */
    public static Map<String, ParserAction> parseRootAction(String[] lines, List<ParserAction> parserActions) {
        TextParser headTextParser = new TextParser(parserActions);
        for (String line : lines) {
            headTextParser.parse(line);
        }
        return headTextParser.getResults();
    }

    /**
     * Parse sub-node actio
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
     * Sub-node parsing
     */
    private static void parseChildNode(ParserAction node) {
        TextParser nextParser = new TextParser(node);
        List<ParserResult> rootResults = node.getRootResults();
        // Merge lines for block
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
     * Convert block text to string.
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
