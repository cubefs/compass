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
 * 自定义注释生成器
 */
public class CommentGenerator extends DefaultCommentGenerator {

    private static final String MODEL_QUERY_CLASS = "Example";
    private static final String MAPPER_CLASS = "Mapper";
    private static final String API_IMPORT_CLASS = "io.swagger.annotations.ApiModelProperty";

    /**
     * 设置用户配置的参数
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
    }

    /**
     * 字段添加swagger注解
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        String remarks = introspectedColumn.getRemarks();
        // sql字段没有注释说明，忽略
        if (!StringUtility.stringHasValue(remarks)) {
            return;
        }
        // 数据库特殊字符"在生成代码注释时和Java字符语法冲突，替换为'
        remarks = remarks.replace("\"", "'");
        // 数据库字段生成实体model类添加swagger注解
        field.addJavaDocLine("@ApiModelProperty(value = \"" + remarks + "\")");
    }

    /**
     * 导入依赖类路径
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        super.addJavaFileComment(compilationUnit);

        if (compilationUnit.getType().getFullyQualifiedName().contains(MODEL_QUERY_CLASS) ||
                compilationUnit.getType().getFullyQualifiedName().contains(MAPPER_CLASS)) {
            return;
        }
        // 实体model类加入swagger
        compilationUnit.addImportedType(new FullyQualifiedJavaType(API_IMPORT_CLASS));
    }
}
