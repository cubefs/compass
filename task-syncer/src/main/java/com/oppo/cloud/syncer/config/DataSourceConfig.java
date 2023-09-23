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

package com.oppo.cloud.syncer.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.oppo.cloud.syncer.domain.Mapping;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * 同步数据表数据
 */
@Data
@Component
@ConfigurationProperties(prefix = "datasource")
@EnableConfigurationProperties(DataSourceConfig.class)
public class DataSourceConfig {

    /**
     * 同步表映射
     */
    private List<Mapping> mappings;

    @Primary
    @Bean(name = "diagnoseDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.diagnose")
    public DataSource masterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "sourceDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.source")
    public DataSource slave1DataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "diagnoseJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("diagnoseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sourceJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("sourceDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
