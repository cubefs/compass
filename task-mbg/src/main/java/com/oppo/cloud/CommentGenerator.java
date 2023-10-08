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

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.DefaultCommentGenerator;

import java.util.Properties;

/**
 * Custom annotation generator
 */
public class CommentGenerator extends DefaultCommentGenerator {

    private static final String MODEL_QUERY_CLASS = "Example";
    private static final String MAPPER_CLASS = "Mapper";
    private static final String API_IMPORT_CLASS = "io.swagger.annotations.ApiModelProperty";

    /**
     * Set configuration parameters
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
    }

    /**
     * Add swagger annotations to fields
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        String remarks = introspectedColumn.getRemarks();
        // The sql field has no annotation and is ignored.
        if (!StringUtility.stringHasValue(remarks)) {
            return;
        }
        // The database special character " conflicts with Java character syntax when generating code comments and is replaced with '
        remarks = remarks.replace("\"", "'");
        // Database fields generate entity model classes and add swagger annotations
        field.addJavaDocLine("@ApiModelProperty(value = \"" + remarks + "\")");
    }

    /**
     * Import dependency classpath
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        super.addJavaFileComment(compilationUnit);

        if (compilationUnit.getType().getFullyQualifiedName().contains(MODEL_QUERY_CLASS) ||
                compilationUnit.getType().getFullyQualifiedName().contains(MAPPER_CLASS)) {
            return;
        }
        // Entity model class added to swagger
        compilationUnit.addImportedType(new FullyQualifiedJavaType(API_IMPORT_CLASS));
    }
}
