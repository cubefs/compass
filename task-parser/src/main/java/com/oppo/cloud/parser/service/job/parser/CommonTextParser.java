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

import com.oppo.cloud.common.util.textparser.*;
import com.oppo.cloud.parser.domain.reader.ReaderObject;
import com.oppo.cloud.parser.service.job.oneclick.OneClickSubject;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@Slf4j
public abstract class CommonTextParser extends OneClickSubject {

    private ReaderObject readerObject;
    private List<ParserAction> actions;

    public Map<String, ParserAction> parse(ReaderObject readerObject, List<ParserAction> actions) throws Exception {
        this.readerObject = readerObject;
        this.actions = actions;

        Map<String, ParserAction> rootActions = getRootAction();
        for (Map.Entry<String, ParserAction> action : rootActions.entrySet()) {
            ParserManager.parseChildActions(action.getValue());
        }

        return rootActions;
    }

    public Map<String, ParserAction> getRootAction() throws Exception {
        TextParser headTextParser = new TextParser(this.actions);
        BufferedReader bufferedReader = this.readerObject.getBufferedReader();
        while (true) {
            String line;
            try {
                line = bufferedReader.readLine();
            } catch (Exception e) {
                log.error("Exception:", e);
                break;
            }
            if (line == null) {
                break;
            }
            headTextParser.parse(line);
        }
        return headTextParser.getResults();
    }

}
