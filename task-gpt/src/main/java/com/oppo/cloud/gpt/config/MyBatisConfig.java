package com.oppo.cloud.gpt.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis Configuration
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.oppo.cloud.mapper", "com.oppo.cloud.gpt.dao"})
public class MyBatisConfig {
}
