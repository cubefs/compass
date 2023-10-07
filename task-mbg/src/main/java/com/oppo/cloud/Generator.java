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

package com.oppo.cloud;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Automatically generate the MBG code corresponding to the MySQL table
 */
public class Generator {

    public static void main(String[] args) throws Exception {
        // MBG Warning information during MBG execution
        List<String> warngins = new ArrayList<>();
        // When the generated code is duplicated, the original code is overwritten
        boolean overwrite = true;
        // Read our MBG configuration file
        InputStream is = Generator.class.getResourceAsStream("/generatorConfig.xml");
        ConfigurationParser cp = new ConfigurationParser(warngins);
        Configuration config = cp.parseConfiguration(is);
        is.close();

        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        // Create MBG
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warngins);
        // Generate
        myBatisGenerator.generate(null);
        // Output warning information
        for (String warning : warngins) {
            System.out.println(warning);
        }
    }
}
