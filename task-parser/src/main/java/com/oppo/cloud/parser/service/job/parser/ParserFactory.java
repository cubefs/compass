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

import com.oppo.cloud.parser.domain.job.ParserParam;
import com.oppo.cloud.parser.service.job.oneclick.IProgressListener;


public class ParserFactory {

    /**
     * create parser
     */
    public static IParser create(ParserParam parserParam, IProgressListener listener) {
        switch (parserParam.getLogType()) {

            case SCHEDULER:
                SchedulerLogParser schedulerLogParser = new SchedulerLogParser(parserParam);
                schedulerLogParser.addListener(listener);
                return schedulerLogParser;

            case SPARK_EVENT:
                SparkEventLogParser sparkEventLogParser = new SparkEventLogParser(parserParam);
                sparkEventLogParser.addListener(listener);
                return sparkEventLogParser;

            case SPARK_EXECUTOR:
                SparkExecutorLogParser sparkExecutorLogParser = new SparkExecutorLogParser(parserParam);
                sparkExecutorLogParser.addListener(listener);
                return sparkExecutorLogParser;

            case MAPREDUCE_JOB_HISTORY:
                MapReduceJobHistoryParser mapReduceJobHistoryParser = new MapReduceJobHistoryParser(parserParam);
                mapReduceJobHistoryParser.addListener(listener);
                return mapReduceJobHistoryParser;

            case MAPREDUCE_CONTAINER:
                MapReduceContainerLogParser mapReduceContainerLogParser = new MapReduceContainerLogParser(parserParam);
                mapReduceContainerLogParser.addListener(listener);
                return mapReduceContainerLogParser;

            default:
                return null;
        }
    }
}
