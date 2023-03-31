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

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oppo.cloud.common.util.textparser.ParserAction;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 解析规则工具类
 */
@Slf4j
public class ParserActionUtil {

    /**
     * 是否包含某个action
     */
    public static boolean containedAction(ParserAction root, String action) {
        Deque<ParserAction> deque = new LinkedList<>();
        ParserAction node;
        deque.push(root);
        while (!deque.isEmpty()) {
            node = deque.pop();
            if (action.equals(node.getAction())) {
                return true;
            }
            List<ParserAction> children = node.getChildren();
            if (children != null) {
                for (int i = children.size() - 1; i >= 0; i--) {
                    deque.push(children.get(i));
                }
            }
        }
        return false;
    }

    /**
     * 获取叶子节点规则列表
     */
    public static List<ParserAction> getLeafAction(ParserAction root, boolean matchSucceed) {
        List<ParserAction> list = new ArrayList<>();
        Deque<ParserAction> nodeDeque = new LinkedList<>();
        ParserAction node = root;
        nodeDeque.push(node);
        while (!nodeDeque.isEmpty()) {
            node = nodeDeque.pop();
            List<ParserAction> children = node.getChildren();
            if (children != null && children.size() > 0) {
                for (int i = children.size() - 1; i >= 0; i--) {
                    ParserAction child = children.get(i);
                    child.setRootResults(node.getParserResults());
                    nodeDeque.push(children.get(i));
                }
            } else if (node.isMatchSucceed() == matchSucceed) {
                list.add(node);
            }

        }
        return list;
    }

    /**
     * 校验规则
     */
    public static ParserAction verifyParserAction(ParserAction action) {
        if (action.isSkip()) {
            log.warn("skip root rule:{}", action.getAction());
            return null;
        }
        traverse(action);
        return action;
    }

    /**
     * 校验规则列表
     */
    public static List<ParserAction> verifyParserActions(List<ParserAction> actions) {
        actions.sort(Comparator.comparing(ParserAction::getStep));
        List<ParserAction> parserActions = new ArrayList<>();
        for (ParserAction action : actions) {
            ParserAction verifyResult = verifyParserAction(action);
            if (verifyResult != null) {
                parserActions.add(action);
            }
        }
        return parserActions;
    }


    /**
     * 遍历规则
     */
    public static void traverse(ParserAction action) {
        Deque<ParserAction> nodeDeque = new LinkedList<>();
        ParserAction node = action;
        nodeDeque.push(node);
        while (!nodeDeque.isEmpty()) {
            node = nodeDeque.pop();
            compileParserTemplate(node);
            List<ParserAction> ruleChildren = node.getChildren();
            if (ruleChildren != null) {
                for (int i = ruleChildren.size() - 1; i >= 0; i--) {
                    ParserAction child = ruleChildren.get(i);
                    if (child.isSkip()) {
                        log.warn("skip child rule:{}", child.getAction());
                        ruleChildren.remove(child);
                        continue;
                    }
                    nodeDeque.push(child);
                }
            }
        }
    }

    /**
     * 正则表达式编译
     */
    private static void compileParserTemplate(ParserAction parserAction) {
        ParserTemplate parserTemplate = parserAction.getParserTemplate();
        if (parserTemplate != null) {
            List<Pattern> heads = parserTemplate.getHeads();
            if (heads != null) {
                List<Pattern> newHeads = new ArrayList<>();
                for (Pattern pattern : heads) {
                    newHeads.add(Pattern.compile(pattern.pattern(), Pattern.DOTALL));
                }
                parserTemplate.setHeads(newHeads);
            }
            List<Pattern> middles = parserTemplate.getMiddles();
            if (middles != null) {
                List<Pattern> newMiddles = new ArrayList<>();
                for (Pattern pattern : middles) {
                    newMiddles.add(Pattern.compile(pattern.pattern(), Pattern.DOTALL));
                }
                parserTemplate.setMiddles(newMiddles);
            }
            List<Pattern> tails = parserTemplate.getTails();
            if (tails != null) {
                List<Pattern> tailsMiddles = new ArrayList<>();
                for (Pattern pattern : tails) {
                    tailsMiddles.add(Pattern.compile(pattern.pattern(), Pattern.DOTALL));
                }
                parserTemplate.setTails(tailsMiddles);
            }
        }
        parserAction.setParserTemplate(parserTemplate);
    }
}
