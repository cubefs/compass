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

import com.oppo.cloud.common.util.textparser.ParserAction;
import com.oppo.cloud.common.util.textparser.ParserActionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ParserActionUtilTest {

    @Test
    void containedAction() {
        Assertions.assertTrue(ParserActionUtil.containedAction(parserAction(), "child1"));
    }

    @Test
    void getLeafAction() {
        List<ParserAction> list = ParserActionUtil.getLeafAction(parserAction(),false);
        list.forEach(data -> {
            System.out.println(data.getAction());
        });

    }

    ParserAction parserAction() {

        ParserAction parserAction = new ParserAction();
        parserAction.setAction("root");

        // 1
        ParserAction child1Action = new ParserAction();
        child1Action.setAction("child1");

        // 1_1
        ParserAction child11Action = new ParserAction();
        child11Action.setAction("child1_1");

        List<ParserAction> child1 = new ArrayList<>();
        child1.add(child11Action);

        child1Action.setChildren(child1);

        ParserAction child2Action = new ParserAction();
        child2Action.setAction("child2");

        List<ParserAction> list = new ArrayList<>();
        list.add(child1Action);
        list.add(child2Action);
        parserAction.setChildren(list);

        return parserAction;
    }
}
